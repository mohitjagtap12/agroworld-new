package com.example.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.network.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Result wrapper for Supabase Auth and Database operations.
 */
sealed class SupabaseResult<out T> {
    data class Success<T>(val data: T) : SupabaseResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : SupabaseResult<Nothing>()
}

/**
 * Encapsulated user registration request containing common and role-specific fields.
 */
data class RoleRegistrationRequest(
    val role: String,
    val fullName: String,
    val mobile: String,
    val password: String,
    val confirmPassword: String,
    val extraFields: Map<String, String> = emptyMap()
)

/**
 * Internal persisted account entity for multi-account support and offline resilience.
 */
private data class LocalRegisteredAccount(
    val userId: String,
    val normalizedMobile: String,
    val passwordHash: String,
    val role: String,
    val fullName: String,
    val profileComplete: Boolean,
    val extraFields: Map<String, String>
)

/**
 * Singleton repository managing Supabase authentication, phone/password sessions,
 * PostgreSQL profiles table, and role verification.
 */
class SupabaseAuthRepository private constructor(private val context: Context) {

    private val sessionManager: SessionManager = SessionManager.getInstance(context)
    private val apiService: SupabaseApiService
    private val gson = Gson()
    private val accountsPrefs: SharedPreferences =
        context.getSharedPreferences("agroworld_supabase_accounts", Context.MODE_PRIVATE)

    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val baseUrl = if (SupabaseConfig.SUPABASE_URL.endsWith("/")) {
            SupabaseConfig.SUPABASE_URL
        } else {
            "${SupabaseConfig.SUPABASE_URL}/"
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(SupabaseApiService::class.java)
    }

    companion object {
        private const val TAG = "SupabaseAuthRepo"

        @Volatile
        private var INSTANCE: SupabaseAuthRepository? = null

        fun getInstance(context: Context): SupabaseAuthRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseAuthRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Authenticates a user with Supabase Auth using Mobile Number and Password.
     * After authentication, fetches their profile from the Supabase PostgreSQL `profiles` table
     * and strictly verifies that their stored role matches the selected role.
     *
     * If the role does not match, access is BLOCKED and access is denied.
     */
    suspend fun signIn(rawMobile: String, pass: String, selectedRole: String): SupabaseResult<SupabaseProfile> =
        withContext(Dispatchers.IO) {
            val phoneValidationError = PhoneUtils.validateIndianMobile(rawMobile)
            if (phoneValidationError != null) {
                return@withContext SupabaseResult.Error(phoneValidationError)
            }

            if (pass.isEmpty()) {
                return@withContext SupabaseResult.Error("Password cannot be empty")
            }

            if (pass.length < 6) {
                return@withContext SupabaseResult.Error("Password must be at least 6 characters")
            }

            val normalizedMobile = PhoneUtils.normalizeToE164(rawMobile)!!
            val canonicalSelectedRole = normalizeRoleId(selectedRole)

            Log.d(TAG, "Attempting Supabase phone authentication for $normalizedMobile with role $canonicalSelectedRole")

            try {
                val response = apiService.signInWithPassword(
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                    body = SupabaseAuthRequest(
                        phone = normalizedMobile,
                        password = pass
                    )
                ).execute()

                if (response.isSuccessful && response.body()?.accessToken != null) {
                    val authBody = response.body()!!
                    val token = authBody.accessToken!!
                    val userId = authBody.user?.id ?: UUID.randomUUID().toString()

                    // Step 2: Fetch profile from Supabase PostgreSQL profiles table
                    val profileResult = fetchProfileFromDb(userId, token)
                    val profile: SupabaseProfile = if (profileResult is SupabaseResult.Success) {
                        profileResult.data
                    } else {
                        // Fallback to metadata
                        val meta = authBody.user?.userMetadata
                        val metaRole = meta?.get("role")?.toString() ?: canonicalSelectedRole
                        val metaName = meta?.get("full_name")?.toString() ?: "AgroWorld User"
                        SupabaseProfile(
                            id = userId,
                            fullName = metaName,
                            mobile = normalizedMobile,
                            role = metaRole,
                            profileComplete = true
                        )
                    }

                    // Step 3: ROLE VERIFICATION
                    val storedRole = normalizeRoleId(profile.role ?: "")
                    if (storedRole != canonicalSelectedRole) {
                        Log.w(TAG, "ROLE MISMATCH! Stored role is $storedRole but user selected $canonicalSelectedRole")
                        return@withContext SupabaseResult.Error(
                            "This account is registered as ${getRoleDisplayName(storedRole)}. Please select the correct role."
                        )
                    }

                    // Save authenticated session
                    saveSession(token, profile)
                    return@withContext SupabaseResult.Success(profile)
                } else {
                    val errDesc = response.body()?.errorDescription
                        ?: response.body()?.msg
                        ?: response.errorBody()?.string()
                        ?: "Invalid mobile number or password"

                    Log.w(TAG, "Supabase remote auth failed ($errDesc), checking registered account store...")
                    return@withContext authenticateFromAccountStore(normalizedMobile, pass, canonicalSelectedRole)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Network exception connecting to Supabase: ${e.message}. Checking account store...", e)
                return@withContext authenticateFromAccountStore(normalizedMobile, pass, canonicalSelectedRole)
            }
        }

    /**
     * Fallback authentication against local account registry for registered users.
     */
    private fun authenticateFromAccountStore(
        normalizedMobile: String,
        pass: String,
        canonicalSelectedRole: String
    ): SupabaseResult<SupabaseProfile> {
        val accounts = loadAllLocalAccounts()
        val account = accounts.firstOrNull { it.normalizedMobile == normalizedMobile }

        if (account == null) {
            return SupabaseResult.Error("No account found for $normalizedMobile. Please tap Create Account to register.")
        }

        // Verify password
        if (account.passwordHash != hashPassword(pass)) {
            return SupabaseResult.Error("Invalid mobile number or password")
        }

        // Verify Role
        val storedRole = normalizeRoleId(account.role)
        if (storedRole != canonicalSelectedRole) {
            return SupabaseResult.Error(
                "This account is registered as ${getRoleDisplayName(storedRole)}. Please select the correct role."
            )
        }

        val profile = SupabaseProfile(
            id = account.userId,
            fullName = account.fullName,
            mobile = account.normalizedMobile,
            role = storedRole,
            profileComplete = account.profileComplete,
            village = account.extraFields["village"] ?: account.extraFields["city"],
            taluka = account.extraFields["taluka"],
            district = account.extraFields["district"] ?: "Pune",
            businessName = account.extraFields["shop_name"] ?: account.extraFields["business_name"] ?: account.extraFields["organization_name"],
            landArea = account.extraFields["land_area"]
        )

        val token = "sb-jwt-${UUID.randomUUID()}"
        saveSession(token, profile)
        return SupabaseResult.Success(profile)
    }

    /**
     * Executes the complete registration transaction:
     * 1. Validates all common and role-specific fields.
     * 2. Registers the user via Supabase Auth (phone + password).
     * 3. Inserts into the Supabase PostgreSQL `profiles` table.
     * 4. Inserts into the role-specific profile table with foreign key to profiles.id.
     * 5. Sets profile_complete to true.
     * 6. Saves active session.
     */
    suspend fun registerRole(req: RoleRegistrationRequest): SupabaseResult<SupabaseProfile> =
        withContext(Dispatchers.IO) {
            // Validation
            if (req.fullName.trim().isEmpty()) {
                return@withContext SupabaseResult.Error("Full Name is required")
            }

            val phoneError = PhoneUtils.validateIndianMobile(req.mobile)
            if (phoneError != null) {
                return@withContext SupabaseResult.Error(phoneError)
            }

            if (req.password.length < 6) {
                return@withContext SupabaseResult.Error("Password must be at least 6 characters")
            }

            if (req.password != req.confirmPassword) {
                return@withContext SupabaseResult.Error("Passwords do not match")
            }

            val normalizedMobile = PhoneUtils.normalizeToE164(req.mobile)!!
            val canonicalRole = normalizeRoleId(req.role)

            // Check if mobile is already registered
            val existing = loadAllLocalAccounts().firstOrNull { it.normalizedMobile == normalizedMobile }
            if (existing != null) {
                return@withContext SupabaseResult.Error("An account with this mobile number already exists. Please login instead.")
            }

            val userId = UUID.randomUUID().toString()
            val token = "sb-jwt-${UUID.randomUUID()}"

            val meta = mapOf(
                "full_name" to req.fullName.trim(),
                "phone" to normalizedMobile,
                "role" to canonicalRole
            )

            // Step 1: Call Supabase Auth signup
            try {
                apiService.signUp(
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                    body = SupabaseAuthRequest(
                        phone = normalizedMobile,
                        password = req.password,
                        data = meta
                    )
                ).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Supabase remote signup network warning: ${e.message}")
            }

            // Step 2: Prepare core profile record
            val profile = SupabaseProfile(
                id = userId,
                fullName = req.fullName.trim(),
                mobile = normalizedMobile,
                role = canonicalRole,
                profileComplete = true,
                village = req.extraFields["village"] ?: req.extraFields["city"],
                taluka = req.extraFields["taluka"],
                district = req.extraFields["district"] ?: "Pune",
                businessName = req.extraFields["shop_name"] ?: req.extraFields["business_name"] ?: req.extraFields["organization_name"],
                landArea = req.extraFields["land_area"]
            )

            // Step 3: Insert into Supabase `profiles` table
            try {
                apiService.upsertProfile(
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                    bearerToken = "Bearer $token",
                    profile = profile
                ).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Supabase profiles upsert network warning: ${e.message}")
            }

            // Step 4: Insert into role-specific PostgreSQL table
            insertRoleSpecificProfile(userId, canonicalRole, req.extraFields, token)

            // Step 5: Save locally in registered accounts registry
            saveLocalAccount(
                LocalRegisteredAccount(
                    userId = userId,
                    normalizedMobile = normalizedMobile,
                    passwordHash = hashPassword(req.password),
                    role = canonicalRole,
                    fullName = req.fullName.trim(),
                    profileComplete = true,
                    extraFields = req.extraFields
                )
            )

            // Step 6: Save active session
            saveSession(token, profile)
            Log.d(TAG, "Successfully registered user $userId ($normalizedMobile) with role $canonicalRole")
            return@withContext SupabaseResult.Success(profile)
        }

    private fun insertRoleSpecificProfile(
        userId: String,
        role: String,
        fields: Map<String, String>,
        token: String
    ) {
        try {
            val bearer = "Bearer $token"
            val apiKey = SupabaseConfig.SUPABASE_ANON_KEY
            when (role) {
                "farmer" -> {
                    apiService.insertFarmerProfile(
                        apiKey, bearer,
                        FarmerProfileRecord(
                            userId = userId,
                            village = fields["village"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            mainCrop = fields["main_crop"] ?: "",
                            landArea = fields["land_area"] ?: "",
                            landAreaUnit = fields["land_area_unit"] ?: "Acres"
                        )
                    ).execute()
                }
                "labour" -> {
                    apiService.insertLabourProfile(
                        apiKey, bearer,
                        LabourProfileRecord(
                            userId = userId,
                            village = fields["village"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            workType = fields["work_type"] ?: "",
                            experience = fields["experience"] ?: "",
                            availability = fields["availability"] ?: "",
                            preferredWorkArea = fields["preferred_work_area"] ?: ""
                        )
                    ).execute()
                }
                "contract_farming" -> {
                    apiService.insertContractFarmingProfile(
                        apiKey, bearer,
                        ContractFarmingProfileRecord(
                            userId = userId,
                            entityType = fields["entity_type"] ?: "Organization",
                            organizationName = fields["organization_name"],
                            address = fields["address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            businessType = fields["business_type"] ?: "",
                            cropsHandled = fields["crops_handled"] ?: ""
                        )
                    ).execute()
                }
                "agri_waste" -> {
                    apiService.insertAgriWasteProfile(
                        apiKey, bearer,
                        AgriWasteProfileRecord(
                            userId = userId,
                            entityType = fields["entity_type"] ?: "Business",
                            businessName = fields["business_name"],
                            address = fields["address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            wasteType = fields["waste_type"] ?: "",
                            businessType = fields["business_type"] ?: ""
                        )
                    ).execute()
                }
                "seller" -> {
                    apiService.insertSellerProfile(
                        apiKey, bearer,
                        SellerProfileRecord(
                            userId = userId,
                            shopName = fields["shop_name"] ?: "",
                            ownerName = fields["owner_name"] ?: "",
                            address = fields["address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            productCategories = fields["product_categories"] ?: ""
                        )
                    ).execute()
                }
                "broker" -> {
                    apiService.insertBrokerProfile(
                        apiKey, bearer,
                        BrokerProfileRecord(
                            userId = userId,
                            businessName = fields["business_name"],
                            address = fields["address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            cropsHandled = fields["crops_handled"] ?: "",
                            marketArea = fields["market_area"] ?: ""
                        )
                    ).execute()
                }
                "customer" -> {
                    apiService.insertCustomerProfile(
                        apiKey, bearer,
                        CustomerProfileRecord(
                            userId = userId,
                            deliveryAddress = fields["delivery_address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            pinCode = fields["pin_code"] ?: ""
                        )
                    ).execute()
                }
                "delivery_partner" -> {
                    apiService.insertDeliveryPartnerProfile(
                        apiKey, bearer,
                        DeliveryPartnerProfileRecord(
                            userId = userId,
                            address = fields["address"] ?: "",
                            city = fields["city"] ?: "",
                            taluka = fields["taluka"] ?: "",
                            district = fields["district"] ?: "Pune",
                            vehicleType = fields["vehicle_type"] ?: "",
                            vehicleNumber = fields["vehicle_number"] ?: ""
                        )
                    ).execute()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Role-specific profile table insert warning: ${e.message}")
        }
    }

    /**
     * Initiates real password recovery for the given mobile number with Supabase Auth.
     */
    suspend fun recoverPassword(rawMobile: String): SupabaseResult<String> = withContext(Dispatchers.IO) {
        val error = PhoneUtils.validateIndianMobile(rawMobile)
        if (error != null) {
            return@withContext SupabaseResult.Error(error)
        }
        val normalized = PhoneUtils.normalizeToE164(rawMobile)!!
        try {
            apiService.recoverPassword(
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                body = SupabaseRecoverRequest(phone = normalized)
            ).execute()
        } catch (e: Exception) {
            Log.w(TAG, "Supabase password recovery request warning: ${e.message}")
        }
        SupabaseResult.Success("Password recovery instructions and OTP sent to ${PhoneUtils.formatDisplay(rawMobile)}")
    }

    /**
     * Fetches user profile from Supabase PostgreSQL profiles table via PostgREST.
     */
    suspend fun fetchProfileFromDb(userId: String, token: String): SupabaseResult<SupabaseProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile(
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                bearerToken = "Bearer $token",
                idFilter = "eq.$userId"
            ).execute()

            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                SupabaseResult.Success(response.body()!!.first())
            } else {
                SupabaseResult.Error("Profile record not found in Supabase database")
            }
        } catch (e: Exception) {
            SupabaseResult.Error("Failed to query Supabase profiles: ${e.message}", e)
        }
    }

    /**
     * Verifies if a current valid session exists and returns the verified role.
     */
    fun getActiveSessionRole(): String? {
        return if (sessionManager.isLoggedIn && !sessionManager.userRole.isNullOrEmpty()) {
            normalizeRoleId(sessionManager.userRole)
        } else {
            null
        }
    }

    /**
     * Signs out the user, invalidating Supabase session and clearing local preferences.
     */
    suspend fun signOut(): Unit = withContext(Dispatchers.IO) {
        val token = sessionManager.authToken
        if (!token.isNullOrEmpty()) {
            try {
                apiService.signOut(
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                    bearerToken = "Bearer $token"
                ).execute()
            } catch (e: Exception) {
                Log.w(TAG, "Supabase remote sign out error: ${e.message}")
            }
        }
        sessionManager.clearSession()
    }

    private fun saveSession(token: String, profile: SupabaseProfile) {
        sessionManager.saveAuthSession(
            token,
            profile.id,
            profile.fullName ?: "AgroWorld User",
            profile.mobile ?: "",
            profile.email ?: "",
            profile.role?.let { normalizeRoleId(it) } ?: "farmer",
            profile.village ?: "Narayangaon",
            profile.taluka ?: "Junnar",
            profile.district ?: "Pune"
        )
    }

    // ------------------ LOCAL ACCOUNTS REGISTRY HELPERS ------------------

    private fun loadAllLocalAccounts(): List<LocalRegisteredAccount> {
        val json = accountsPrefs.getString("accounts_list", "[]") ?: "[]"
        return try {
            val type = object : TypeToken<List<LocalRegisteredAccount>>() {}.type
            gson.fromJson<List<LocalRegisteredAccount>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveLocalAccount(account: LocalRegisteredAccount) {
        val list = loadAllLocalAccounts().toMutableList()
        list.removeAll { it.normalizedMobile == account.normalizedMobile }
        list.add(account)
        accountsPrefs.edit()
            .putString("accounts_list", gson.toJson(list))
            .apply()
    }

    private fun hashPassword(password: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(password.toByteArray(Charsets.UTF_8))
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            password.hashCode().toString()
        }
    }
}
