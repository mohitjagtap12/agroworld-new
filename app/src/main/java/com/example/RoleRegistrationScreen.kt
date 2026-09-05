package com.example

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.supabase.PhoneUtils
import com.example.supabase.RoleRegistrationRequest
import com.example.supabase.SupabaseAuthRepository
import com.example.supabase.SupabaseResult
import com.example.supabase.getRoleDisplayName
import com.example.supabase.normalizeRoleId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleRegistrationScreen(
    role: String,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val canonicalRole = remember(role) { normalizeRoleId(role) }
    val roleDisplayName = remember(canonicalRole) { getRoleDisplayName(canonicalRole) }

    // Common fields
    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Role-specific fields
    // Common location fields
    var villageOrCity by remember { mutableStateOf("") }
    var taluka by remember { mutableStateOf("Junnar") }
    var district by remember { mutableStateOf("Pune") }
    var address by remember { mutableStateOf("") }

    // Farmer specific
    var mainCrop by remember { mutableStateOf("") }
    var landArea by remember { mutableStateOf("") }
    var landAreaUnit by remember { mutableStateOf("Acres") }

    // Labour specific
    var workType by remember { mutableStateOf("Harvesting") }
    var experience by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Immediate") }
    var preferredWorkArea by remember { mutableStateOf("") }

    // Contract Farming & Agri Waste specific
    var entityType by remember { mutableStateOf("Organization") }
    var organizationOrBusinessName by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("") }
    var cropsHandled by remember { mutableStateOf("") }
    var wasteType by remember { mutableStateOf("") }

    // Seller specific
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var productCategories by remember { mutableStateOf("") }

    // Broker specific
    var marketArea by remember { mutableStateOf("APMC Narayangaon") }

    // Customer specific
    var pinCode by remember { mutableStateOf("") }

    // Delivery Partner specific
    var vehicleType by remember { mutableStateOf("Mini Truck / Pickup") }
    var vehicleNumber by remember { mutableStateOf("") }

    // State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$roleDisplayName Registration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("reg_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Login"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1B5E20),
                    navigationIconContentColor = Color(0xFF1B5E20)
                )
            )
        },
        containerColor = Color(0xFFF7F9F6),
        modifier = Modifier.testTag("role_registration_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "New $roleDisplayName Account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1B5E20)
                        )
                        Text(
                            text = "Role is locked to $roleDisplayName for security",
                            fontSize = 12.sp,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Banner
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFC62828),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // SECTION 1: ACCOUNT CREDENTIALS
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Account Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF2E7D32)
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Ramesh Patil") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF2E7D32)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                    )

                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() || ch == '+' || ch == ' ' || ch == '-' }) {
                                mobileNumber = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Mobile Number (10 Digits) *") },
                        placeholder = { Text("e.g. 9876543210") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2E7D32)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_mobile_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password (min 6 chars) *") },
                        placeholder = { Text("Enter secure password") },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32)) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password *") },
                        placeholder = { Text("Re-enter password") },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF2E7D32)) },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: ROLE SPECIFIC INFORMATION
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "$roleDisplayName Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF2E7D32)
                    )

                    when (canonicalRole) {
                        // ---------------- FARMER ----------------
                        "farmer" -> {
                            OutlinedTextField(
                                value = villageOrCity,
                                onValueChange = { villageOrCity = it },
                                label = { Text("Village / Town *") },
                                placeholder = { Text("e.g. Narayangaon") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("farmer_village_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = taluka,
                                    onValueChange = { taluka = it },
                                    label = { Text("Taluka") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("farmer_taluka_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("farmer_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = mainCrop,
                                onValueChange = { mainCrop = it },
                                label = { Text("Main Crop *") },
                                placeholder = { Text("e.g. Onion, Tomato, Grapes, Sugarcane") },
                                leadingIcon = { Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("farmer_main_crop_input")
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = landArea,
                                    onValueChange = { landArea = it },
                                    label = { Text("Land Area *") },
                                    placeholder = { Text("e.g. 5") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.3f).testTag("farmer_land_area_input")
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Unit", fontSize = 12.sp, color = Color.Gray)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Acres", "Guntha").forEach { unit ->
                                            FilterChip(
                                                selected = landAreaUnit == unit,
                                                onClick = { landAreaUnit = unit },
                                                label = { Text(unit, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ---------------- LABOUR ----------------
                        "labour" -> {
                            OutlinedTextField(
                                value = villageOrCity,
                                onValueChange = { villageOrCity = it },
                                label = { Text("Village / Town *") },
                                placeholder = { Text("e.g. Narayangaon") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("labour_village_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = taluka,
                                    onValueChange = { taluka = it },
                                    label = { Text("Taluka") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("labour_taluka_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("labour_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = workType,
                                onValueChange = { workType = it },
                                label = { Text("Work Type / Specialization *") },
                                placeholder = { Text("e.g. Harvesting, Sowing, Spraying, General Labour") },
                                leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("labour_work_type_input")
                            )

                            OutlinedTextField(
                                value = experience,
                                onValueChange = { experience = it },
                                label = { Text("Experience *") },
                                placeholder = { Text("e.g. 3 years") },
                                leadingIcon = { Icon(Icons.Default.WorkHistory, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("labour_experience_input")
                            )

                            OutlinedTextField(
                                value = availability,
                                onValueChange = { availability = it },
                                label = { Text("Availability *") },
                                placeholder = { Text("e.g. Immediate, Full-time, Seasonal") },
                                leadingIcon = { Icon(Icons.Default.EventAvailable, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("labour_availability_input")
                            )

                            OutlinedTextField(
                                value = preferredWorkArea,
                                onValueChange = { preferredWorkArea = it },
                                label = { Text("Preferred Work Area / Radius *") },
                                placeholder = { Text("e.g. Junnar taluka & surrounding 15km") },
                                leadingIcon = { Icon(Icons.Default.NearMe, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("labour_work_area_input")
                            )
                        }

                        // ---------------- CONTRACT FARMING ----------------
                        "contract_farming" -> {
                            Text("Entity Classification", fontSize = 12.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("Organization", "Individual").forEach { type ->
                                    FilterChip(
                                        selected = entityType == type,
                                        onClick = { entityType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }

                            if (entityType == "Organization") {
                                OutlinedTextField(
                                    value = organizationOrBusinessName,
                                    onValueChange = { organizationOrBusinessName = it },
                                    label = { Text("Organization / Company Name *") },
                                    placeholder = { Text("e.g. AgroCorp India Pvt Ltd") },
                                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("contract_org_input")
                                )
                            }

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Office / Facility Address *") },
                                placeholder = { Text("Building, Street address") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("contract_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town *") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("contract_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("contract_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = businessType,
                                onValueChange = { businessType = it },
                                label = { Text("Farming / Business Type *") },
                                placeholder = { Text("e.g. Direct Procurement, Food Processing") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("contract_business_type_input")
                            )

                            OutlinedTextField(
                                value = cropsHandled,
                                onValueChange = { cropsHandled = it },
                                label = { Text("Crops Handled *") },
                                placeholder = { Text("e.g. Potato, Baby Corn, Grapes, Soybean") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("contract_crops_input")
                            )
                        }

                        // ---------------- AGRI WASTE ----------------
                        "agri_waste" -> {
                            Text("Entity Classification", fontSize = 12.sp, color = Color.Gray)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                listOf("Business", "Individual").forEach { type ->
                                    FilterChip(
                                        selected = entityType == type,
                                        onClick = { entityType = type },
                                        label = { Text(type) }
                                    )
                                }
                            }

                            if (entityType == "Business") {
                                OutlinedTextField(
                                    value = organizationOrBusinessName,
                                    onValueChange = { organizationOrBusinessName = it },
                                    label = { Text("Business / Plant Name *") },
                                    placeholder = { Text("e.g. GreenBio Fuel Plant") },
                                    leadingIcon = { Icon(Icons.Default.Recycling, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("agri_waste_business_input")
                                )
                            }

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Facility Address *") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("agri_waste_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("agri_waste_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("agri_waste_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = wasteType,
                                onValueChange = { wasteType = it },
                                label = { Text("Waste Type Handled *") },
                                placeholder = { Text("e.g. Biomass, Bagasse, Rice Husk, Cow Dung") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("agri_waste_type_input")
                            )

                            OutlinedTextField(
                                value = businessType,
                                onValueChange = { businessType = it },
                                label = { Text("Business Type *") },
                                placeholder = { Text("e.g. Biomass Pellets, Composting, Buyer") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("agri_waste_category_input")
                            )
                        }

                        // ---------------- SELLER ----------------
                        "seller" -> {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("Shop / Krushi Seva Kendra Name *") },
                                placeholder = { Text("e.g. Kisan Krushi Kendra") },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("seller_shop_input")
                            )

                            OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text("Owner Name *") },
                                placeholder = { Text("e.g. Santosh Jadhav") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("seller_owner_input")
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Shop Address *") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("seller_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("seller_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("seller_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = productCategories,
                                onValueChange = { productCategories = it },
                                label = { Text("Product Categories *") },
                                placeholder = { Text("e.g. Seeds, Fertilizers, Pesticides, Irrigation Tools") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("seller_categories_input")
                            )
                        }

                        // ---------------- BROKER ----------------
                        "broker" -> {
                            OutlinedTextField(
                                value = organizationOrBusinessName,
                                onValueChange = { organizationOrBusinessName = it },
                                label = { Text("Agency / Trading Firm Name") },
                                placeholder = { Text("e.g. Patil Commission Agent") },
                                leadingIcon = { Icon(Icons.Default.Handshake, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("broker_firm_input")
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("APMC Stall / Office Address *") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("broker_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("broker_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("broker_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = cropsHandled,
                                onValueChange = { cropsHandled = it },
                                label = { Text("Crops Traded *") },
                                placeholder = { Text("e.g. Onion, Tomato, Pomegranate, Banana") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("broker_crops_input")
                            )

                            OutlinedTextField(
                                value = marketArea,
                                onValueChange = { marketArea = it },
                                label = { Text("Market / Mandi Area *") },
                                placeholder = { Text("e.g. APMC Narayangaon, Pune Market Yard") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("broker_market_input")
                            )
                        }

                        // ---------------- CUSTOMER ----------------
                        "customer" -> {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Delivery Address *") },
                                placeholder = { Text("House / Flat No, Street, Landmark") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("customer_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town *") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("customer_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("customer_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 6) pinCode = it },
                                label = { Text("PIN Code (6 Digits) *") },
                                placeholder = { Text("e.g. 410504") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("customer_pincode_input")
                            )
                        }

                        // ---------------- DELIVERY PARTNER ----------------
                        "delivery_partner" -> {
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Base Address *") },
                                placeholder = { Text("Operating location address") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("delivery_address_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = villageOrCity,
                                    onValueChange = { villageOrCity = it },
                                    label = { Text("City / Town") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("delivery_city_input")
                                )
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = { district = it },
                                    label = { Text("District") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("delivery_district_input")
                                )
                            }

                            OutlinedTextField(
                                value = vehicleType,
                                onValueChange = { vehicleType = it },
                                label = { Text("Vehicle Type *") },
                                placeholder = { Text("e.g. Mini Truck / Pickup, Three Wheeler") },
                                leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("delivery_vehicle_type_input")
                            )

                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { vehicleNumber = it.uppercase() },
                                label = { Text("Vehicle Registration Number *") },
                                placeholder = { Text("e.g. MH-14-GH-1234") },
                                leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("delivery_vehicle_num_input")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // REGISTER ACTION BUTTON
            Button(
                onClick = {
                    // Common validations
                    if (fullName.trim().isEmpty()) {
                        errorMessage = "Please enter your Full Name"
                        return@Button
                    }
                    val mobileError = PhoneUtils.validateIndianMobile(mobileNumber)
                    if (mobileError != null) {
                        errorMessage = mobileError
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    // Build role extra fields map
                    val extraFields = mutableMapOf<String, String>()
                    when (canonicalRole) {
                        "farmer" -> {
                            if (villageOrCity.isBlank()) {
                                errorMessage = "Village is required"
                                return@Button
                            }
                            if (mainCrop.isBlank()) {
                                errorMessage = "Main Crop is required"
                                return@Button
                            }
                            if (landArea.isBlank()) {
                                errorMessage = "Land Area is required"
                                return@Button
                            }
                            extraFields["village"] = villageOrCity.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["main_crop"] = mainCrop.trim()
                            extraFields["land_area"] = landArea.trim()
                            extraFields["land_area_unit"] = landAreaUnit.trim()
                        }
                        "labour" -> {
                            if (villageOrCity.isBlank()) {
                                errorMessage = "Village is required"
                                return@Button
                            }
                            if (workType.isBlank()) {
                                errorMessage = "Work Type is required"
                                return@Button
                            }
                            extraFields["village"] = villageOrCity.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["work_type"] = workType.trim()
                            extraFields["experience"] = experience.ifBlank { "1 year" }.trim()
                            extraFields["availability"] = availability.ifBlank { "Immediate" }.trim()
                            extraFields["preferred_work_area"] = preferredWorkArea.ifBlank { "$taluka area" }.trim()
                        }
                        "contract_farming" -> {
                            extraFields["entity_type"] = entityType
                            extraFields["organization_name"] = organizationOrBusinessName.trim()
                            extraFields["address"] = address.ifBlank { "Narayangaon" }.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["business_type"] = businessType.ifBlank { "Contract Procurement" }.trim()
                            extraFields["crops_handled"] = cropsHandled.ifBlank { "Various" }.trim()
                        }
                        "agri_waste" -> {
                            extraFields["entity_type"] = entityType
                            extraFields["business_name"] = organizationOrBusinessName.trim()
                            extraFields["address"] = address.ifBlank { "Narayangaon" }.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["waste_type"] = wasteType.ifBlank { "Biomass" }.trim()
                            extraFields["business_type"] = businessType.ifBlank { "Buyer" }.trim()
                        }
                        "seller" -> {
                            extraFields["shop_name"] = shopName.ifBlank { "$fullName Agro Services" }.trim()
                            extraFields["owner_name"] = ownerName.ifBlank { fullName }.trim()
                            extraFields["address"] = address.ifBlank { "Market Yard" }.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["product_categories"] = productCategories.ifBlank { "Seeds & Fertilizers" }.trim()
                        }
                        "broker" -> {
                            extraFields["business_name"] = organizationOrBusinessName.ifBlank { "$fullName Trading" }.trim()
                            extraFields["address"] = address.ifBlank { "APMC Yard" }.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["crops_handled"] = cropsHandled.ifBlank { "Onion, Tomato" }.trim()
                            extraFields["market_area"] = marketArea.ifBlank { "APMC Narayangaon" }.trim()
                        }
                        "customer" -> {
                            if (address.isBlank()) {
                                errorMessage = "Delivery address is required"
                                return@Button
                            }
                            extraFields["delivery_address"] = address.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["pin_code"] = pinCode.ifBlank { "410504" }.trim()
                        }
                        "delivery_partner" -> {
                            extraFields["address"] = address.ifBlank { "Narayangaon" }.trim()
                            extraFields["city"] = villageOrCity.ifBlank { "Pune" }.trim()
                            extraFields["taluka"] = taluka.trim()
                            extraFields["district"] = district.trim()
                            extraFields["vehicle_type"] = vehicleType.trim()
                            extraFields["vehicle_number"] = vehicleNumber.ifBlank { "MH-14-AG-1001" }.trim()
                        }
                    }

                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null

                        val req = RoleRegistrationRequest(
                            role = canonicalRole,
                            fullName = fullName.trim(),
                            mobile = mobileNumber.trim(),
                            password = password,
                            confirmPassword = confirmPassword,
                            extraFields = extraFields
                        )

                        val result = SupabaseAuthRepository.getInstance(context).registerRole(req)
                        isLoading = false

                        when (result) {
                            is SupabaseResult.Success -> {
                                Toast.makeText(context, "Account created successfully for $roleDisplayName!", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard/$canonicalRole") {
                                    popUpTo("role_selection") { inclusive = false }
                                }
                            }
                            is SupabaseResult.Error -> {
                                errorMessage = result.message
                            }
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "REGISTER AS $roleDisplayName",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch back to Login
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { navController.popBackStack() }
                    .padding(8.dp)
                    .testTag("reg_to_login_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = Color(0xFF616161)
                )
                Text(
                    text = "Sign In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
