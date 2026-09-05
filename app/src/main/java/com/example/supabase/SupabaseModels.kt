package com.example.supabase

import com.google.gson.annotations.SerializedName

/**
 * Data model for Supabase Auth request payloads and responses supporting Phone & Password.
 */
data class SupabaseAuthRequest(
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: Map<String, Any>? = null
)

data class SupabaseRecoverRequest(
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)

data class SupabaseAuthResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token_type") val tokenType: String? = "bearer",
    @SerializedName("expires_in") val expiresIn: Long? = 3600,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("user") val user: SupabaseUser? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("error_description") val errorDescription: String? = null,
    @SerializedName("msg") val msg: String? = null
)

data class SupabaseUser(
    @SerializedName("id") val id: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("user_metadata") val userMetadata: Map<String, Any>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/**
 * Data model representing the core Supabase PostgreSQL `profiles` table.
 *
 * Schema:
 *   id: UUID primary key references auth.users(id)
 *   full_name: TEXT NOT NULL
 *   mobile: TEXT NOT NULL UNIQUE
 *   role: TEXT NOT NULL (farmer, labour, contract_farming, agri_waste, seller, broker, customer, delivery_partner)
 *   profile_complete: BOOLEAN DEFAULT FALSE
 *   created_at: TIMESTAMPTZ DEFAULT NOW()
 *   updated_at: TIMESTAMPTZ DEFAULT NOW()
 */
data class SupabaseProfile(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("profile_complete") val profileComplete: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,

    // Auxiliary cached attributes for quick UI binding
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = mobile,
    @SerializedName("village") val village: String? = null,
    @SerializedName("taluka") val taluka: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("land_area") val landArea: String? = null,
    @SerializedName("business_name") val businessName: String? = null
)

// ------------------ ROLE-SPECIFIC POSTGRESQL MODELS ------------------

data class FarmerProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("village") val village: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("main_crop") val mainCrop: String,
    @SerializedName("land_area") val landArea: String,
    @SerializedName("land_area_unit") val landAreaUnit: String = "Acres",
    @SerializedName("created_at") val createdAt: String? = null
)

data class LabourProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("village") val village: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("work_type") val workType: String,
    @SerializedName("experience") val experience: String,
    @SerializedName("availability") val availability: String,
    @SerializedName("preferred_work_area") val preferredWorkArea: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ContractFarmingProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("entity_type") val entityType: String, // Individual or Organization
    @SerializedName("organization_name") val organizationName: String? = null,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("business_type") val businessType: String,
    @SerializedName("crops_handled") val cropsHandled: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class AgriWasteProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("entity_type") val entityType: String, // Individual or Business
    @SerializedName("business_name") val businessName: String? = null,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("waste_type") val wasteType: String,
    @SerializedName("business_type") val businessType: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class SellerProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("shop_name") val shopName: String,
    @SerializedName("owner_name") val ownerName: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("product_categories") val productCategories: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class BrokerProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("business_name") val businessName: String? = null,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("crops_handled") val cropsHandled: String,
    @SerializedName("market_area") val marketArea: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class CustomerProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("pin_code") val pinCode: String,
    @SerializedName("created_at") val createdAt: String? = null
)

data class DeliveryPartnerProfileRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("taluka") val taluka: String,
    @SerializedName("district") val district: String = "Pune",
    @SerializedName("vehicle_type") val vehicleType: String,
    @SerializedName("vehicle_number") val vehicleNumber: String,
    @SerializedName("created_at") val createdAt: String? = null
)

