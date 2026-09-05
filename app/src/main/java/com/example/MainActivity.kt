package com.example

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.network.SessionManager
import com.example.supabase.PhoneUtils
import com.example.supabase.SupabaseAuthRepository
import com.example.supabase.SupabaseResult
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        NavHost(
          navController = navController,
          startDestination = "splash",
          modifier = Modifier.fillMaxSize()
        ) {
          composable("splash") {
            SplashScreen(navController = navController)
          }
          composable("role_selection") {
            RoleSelectionScreen(navController = navController)
          }
          composable(
            route = "login?role={role}",
            arguments = listOf(navArgument("role") {
              type = NavType.StringType
              defaultValue = ""
            })
          ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            LoginScreen(navController = navController, preselectedRole = role)
          }
          composable(
            route = "login/{role}",
            arguments = listOf(navArgument("role") {
              type = NavType.StringType
            })
          ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            LoginScreen(navController = navController, preselectedRole = role)
          }
          composable("login") {
            LoginScreen(navController = navController, preselectedRole = "")
          }
          composable(
            route = "register?role={role}",
            arguments = listOf(navArgument("role") {
              type = NavType.StringType
              defaultValue = "farmer"
            })
          ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"
            RoleRegistrationScreen(role = role, navController = navController)
          }
          composable(
            route = "register/{role}",
            arguments = listOf(navArgument("role") {
              type = NavType.StringType
            })
          ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "farmer"
            RoleRegistrationScreen(role = role, navController = navController)
          }
          composable(
            route = "dashboard/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
          ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "customer"
            when (role.lowercase()) {
              "farmer" -> FarmerPortalScreen(navController = navController)
              "customer" -> CustomerPortalScreen(navController = navController)
              "broker" -> BrokerPortalScreen(navController = navController)
              "seller" -> SellerPortalScreen(navController = navController)
              "labour", "farm_squad", "labour_squad" -> LabourPortalScreen(navController = navController)
              "company", "contract_farming" -> CompanyPortalScreen(navController = navController)
              "delivery", "delivery_partner" -> DeliveryPartnerPortalScreen(navController = navController)
              "waste", "agri_waste", "waste_buyer" -> AgriWasteMarketplaceScreen(navController = navController, initialMode = "buyer")
              else -> DashboardScreen(role = role, navController = navController)
            }
          }
        }
      }
    }
  }
}

// ------------------ SPLASH SCREEN ------------------
@Composable
fun SplashScreen(navController: NavController) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(1600)
    val activeRole = SupabaseAuthRepository.getInstance(context).getActiveSessionRole()
    if (!activeRole.isNullOrEmpty()) {
      navController.navigate("dashboard/$activeRole") {
        popUpTo("splash") { inclusive = true }
      }
    } else {
      navController.navigate("role_selection") {
        popUpTo("splash") { inclusive = true }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFFFDFBFF),
            Color(0xFFE7F0FF),
            Color(0xFFD3E4FF)
          )
        )
      )
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      Box(
        modifier = Modifier
          .size(130.dp)
          .clip(CircleShape)
          .background(Color.White, shape = CircleShape)
          .drawBehind {
            drawCircle(
              color = Color(0xFF0061A4).copy(alpha = 0.15f),
              radius = size.maxDimension / 2 + 12.dp.toPx()
            )
          },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Eco,
          contentDescription = "AgroWorld Leaf Logo",
          tint = Color(0xFF0061A4),
          modifier = Modifier.size(72.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "AgroWorld",
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0061A4),
        letterSpacing = 1.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Connecting Farmers, Buyers & Agricultural Services",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF44474E),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 24.dp)
      )

      Spacer(modifier = Modifier.height(64.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color(0xFF0061A4),
          strokeWidth = 2.5.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Initializing Field Access...",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF0061A4),
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

// ------------------ HELPERS FOR ROLE MAPPING & AUTH ------------------
fun getRoleDisplayName(roleId: String): String {
  return when (roleId.lowercase()) {
    "farmer" -> "Farmer"
    "labour", "farm_squad", "labour_squad" -> "Labour / Farm Squad"
    "company", "contract_farming" -> "Contract Farming"
    "waste", "agri_waste", "waste_buyer" -> "Agri Waste"
    "seller" -> "Seller"
    "broker" -> "Broker"
    "customer" -> "Customer"
    "delivery", "delivery_partner" -> "Delivery Partner"
    else -> if (roleId.isBlank()) "AgroWorld" else roleId.replaceFirstChar { it.uppercase() }
  }
}

fun getRoleIcon(roleId: String): ImageVector {
  return when (roleId.lowercase()) {
    "farmer" -> Icons.Default.Agriculture
    "labour", "farm_squad", "labour_squad" -> Icons.Default.Engineering
    "company", "contract_farming" -> Icons.Default.Handshake
    "waste", "agri_waste", "waste_buyer" -> Icons.Default.Recycling
    "seller" -> Icons.Default.Storefront
    "broker" -> Icons.Default.TrendingUp
    "customer" -> Icons.Default.ShoppingCart
    "delivery", "delivery_partner" -> Icons.Default.LocalShipping
    else -> Icons.Default.Agriculture
  }
}

// ------------------ LOGIN SCREEN (SUPABASE MOBILE AUTHENTICATION) ------------------
@Composable
fun LoginScreen(navController: NavController, preselectedRole: String = "") {
  val effectiveRole = if (preselectedRole.isNotBlank()) preselectedRole.lowercase() else "farmer"
  val roleDisplayName = getRoleDisplayName(effectiveRole)
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var mobileNumber by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showForgotPasswordDialog by remember { mutableStateOf(false) }
  var forgotPasswordMobile by remember { mutableStateOf("") }
  var isRecovering by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FBF7))
      .testTag("login_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation Bar with Back Button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              Log.d("AgroWorldNav", "BACK_CLICKED: returning to role selection from $effectiveRole login")
              navController.popBackStack()
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("back_button"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to role selection",
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Back",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E7D32)
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFE8F5E9),
          border = BorderStroke(1.dp, Color(0xFFC8E6C9))
        ) {
          Text(
            text = roleDisplayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Role Icon Avatar Header
      Box(
        modifier = Modifier
          .size(80.dp)
          .clip(CircleShape)
          .background(Color(0xFF2E7D32)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = getRoleIcon(effectiveRole),
          contentDescription = "$roleDisplayName Icon",
          tint = Color.White,
          modifier = Modifier.size(42.dp)
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Dynamic Title: "<Role> Login"
      Text(
        text = "$roleDisplayName Login",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1B1D1B),
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("login_title")
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Sign in to your account with Supabase authentication",
        fontSize = 14.sp,
        color = Color(0xFF616161),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Error banner if any
      errorMessage?.let { msg ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFEBEE),
          border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFC62828),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = msg,
              fontSize = 13.sp,
              color = Color(0xFFC62828),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Card Container for Credentials
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Mobile Number Field
          OutlinedTextField(
            value = mobileNumber,
            onValueChange = {
              if (it.all { ch -> ch.isDigit() || ch == '+' || ch == ' ' || ch == '-' }) {
                mobileNumber = it
                errorMessage = null
              }
            },
            label = { Text("Mobile Number") },
            placeholder = { Text("e.g. 9876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = {
              Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2E7D32))
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("phone_input")
          )

          // Password Field
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            label = { Text("Password") },
            placeholder = { Text("Enter password") },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
              Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32))
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                  tint = Color(0xFF757575)
                )
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("password_input")
          )

          // Forgot Password Link
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
          ) {
            Text(
              text = "Forgot Password?",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF2E7D32),
              modifier = Modifier
                .clickable {
                  forgotPasswordMobile = mobileNumber
                  showForgotPasswordDialog = true
                }
                .testTag("forgot_password_button")
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Primary Action: LOGIN Button
          Button(
            onClick = {
              val mobileValidation = PhoneUtils.validateIndianMobile(mobileNumber)
              if (mobileValidation != null) {
                errorMessage = mobileValidation
                return@Button
              }
              if (password.isBlank()) {
                errorMessage = "Please enter your password"
                return@Button
              }
              if (password.length < 6) {
                errorMessage = "Password must be at least 6 characters"
                return@Button
              }

              coroutineScope.launch {
                isLoading = true
                errorMessage = null

                val result = SupabaseAuthRepository.getInstance(context).signIn(
                  rawMobile = mobileNumber.trim(),
                  pass = password.trim(),
                  selectedRole = effectiveRole
                )
                isLoading = false

                when (result) {
                  is SupabaseResult.Success -> {
                    val profile = result.data
                    val destRole = profile.role ?: effectiveRole
                    Log.d("AgroWorldNav", "AUTHENTICATED: ${profile.mobile} as $destRole")
                    Toast.makeText(context, "Logged in as ${profile.fullName ?: roleDisplayName}", Toast.LENGTH_SHORT).show()
                    navController.navigate("dashboard/$destRole") {
                      popUpTo("role_selection") { inclusive = false }
                    }
                  }
                  is SupabaseResult.Error -> {
                    Log.e("AgroWorldNav", "SIGN_IN_ERROR: ${result.message}")
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
              .testTag("login_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
              )
            } else {
              Text(
                text = "LOGIN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Switch to Create Account / Registration for Selected Role
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable {
            errorMessage = null
            navController.navigate("register?role=$effectiveRole")
          }
          .padding(8.dp)
          .testTag("create_account_button"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Don't have an account? ",
          fontSize = 14.sp,
          color = Color(0xFF616161)
        )
        Text(
          text = "Create Account",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF2E7D32)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Secure Supabase Authentication • AgroWorld Platform",
        fontSize = 11.sp,
        color = Color(0xFF757575)
      )
    }
  }

  // Forgot Password Dialog
  if (showForgotPasswordDialog) {
    AlertDialog(
      onDismissRequest = { showForgotPasswordDialog = false },
      title = { Text("Reset Password") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Enter your registered Indian mobile number to receive password recovery instructions via Supabase Auth.",
            fontSize = 14.sp,
            color = Color(0xFF616161)
          )
          OutlinedTextField(
            value = forgotPasswordMobile,
            onValueChange = { forgotPasswordMobile = it },
            label = { Text("Mobile Number") },
            placeholder = { Text("e.g. 9876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val phoneErr = PhoneUtils.validateIndianMobile(forgotPasswordMobile)
            if (phoneErr != null) {
              Toast.makeText(context, phoneErr, Toast.LENGTH_SHORT).show()
              return@Button
            }
            coroutineScope.launch {
              isRecovering = true
              val res = SupabaseAuthRepository.getInstance(context).recoverPassword(forgotPasswordMobile)
              isRecovering = false
              showForgotPasswordDialog = false
              when (res) {
                is SupabaseResult.Success -> {
                  Toast.makeText(context, res.data, Toast.LENGTH_LONG).show()
                }
                is SupabaseResult.Error -> {
                  Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                }
              }
            }
          },
          enabled = !isRecovering,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
          if (isRecovering) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
          } else {
            Text("Send Recovery Link", color = Color.White)
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showForgotPasswordDialog = false }) {
          Text("Cancel", color = Color(0xFF616161))
        }
      }
    )
  }
}

// ------------------ ROLE SELECTION SCREEN ------------------
data class RoleInfo(
  val id: String,
  val name: String,
  val description: String,
  val icon: ImageVector
)

@Composable
fun RoleSelectionScreen(navController: NavController) {
  var selectedRoleId by remember { mutableStateOf<String?>(null) }

  val roles = listOf(
    RoleInfo(
      id = "farmer",
      name = "Farmer",
      description = "Manage crops, detect diseases with AI, hire labour, sell produce & waste, and buy farm supplies.",
      icon = Icons.Default.Agriculture
    ),
    RoleInfo(
      id = "seller",
      name = "Agri-Store Seller",
      description = "List certified seeds, fertilizers, pesticides, and modern farming equipment to local farmers.",
      icon = Icons.Default.Storefront
    ),
    RoleInfo(
      id = "labour",
      name = "Labour & Farm Squad",
      description = "Receive hiring requests from farmers for harvesting, spraying, tilling, and planting with daily wage tracking.",
      icon = Icons.Default.Engineering
    ),
    RoleInfo(
      id = "company",
      name = "Contract Farming Company",
      description = "Publish institutional crop contracts with assured buyback prices, review farmer applications, and manage harvests.",
      icon = Icons.Default.Handshake
    ),
    RoleInfo(
      id = "broker",
      name = "APMC Broker / Trader",
      description = "Broadcast bulk crop demands, negotiate wholesale prices with farmers, and close high-volume deals.",
      icon = Icons.Default.TrendingUp
    ),
    RoleInfo(
      id = "customer",
      name = "Customer / Direct Buyer",
      description = "Shop fresh farm-direct fruits, vegetables, and grains directly from verified local growers.",
      icon = Icons.Default.ShoppingCart
    ),
    RoleInfo(
      id = "waste",
      name = "Agri Waste & Biomass Buyer",
      description = "Source wheat & rice straw, sugarcane trash, maize stalks, and coconut husks directly from farmers for biofuel, feed, & compost.",
      icon = Icons.Default.Recycling
    ),
    RoleInfo(
      id = "delivery",
      name = "Delivery Partner",
      description = "Accept farm-to-table delivery jobs for produce, fertilizers, equipment, and biomass waste.",
      icon = Icons.Default.LocalShipping
    )
  )

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val windowWidth = maxWidth
    val isDesktop = windowWidth >= 1024.dp
    val isTablet = windowWidth in 600.dp..1023.dp

    Scaffold(
      topBar = {
        Surface(
          tonalElevation = 1.dp,
          color = Color(0xFFF8FBF7),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .height(60.dp)
              .padding(horizontal = if (isDesktop) 32.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = "AgroWorld Brand Icon",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(30.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "AgroWorld",
                fontSize = if (isDesktop) 22.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Text(
                text = "Market",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.clickable { }
              )
              Text(
                text = "Help",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161),
                modifier = Modifier.clickable { }
              )
            }
          }
        }
      },
      bottomBar = {
        Surface(
          color = Color(0xFFF8FBF7),
          tonalElevation = 4.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .navigationBarsPadding()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth(),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Button(
                onClick = {
                  selectedRoleId?.let { roleId ->
                    Log.d("AgroWorldNav", "ROLE_SELECTED: $roleId")
                    Log.d("AgroWorldNav", "OPENING_LOGIN: $roleId")
                    navController.navigate("login?role=$roleId")
                  }
                },
                enabled = selectedRoleId != null,
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF2E7D32),
                  contentColor = Color.White,
                  disabledContainerColor = Color(0xFFC4C6D0).copy(alpha = 0.4f),
                  disabledContentColor = Color(0xFF1A1C1E).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(56.dp)
                  .testTag("continue_button")
              ) {
                Text(
                  text = if (selectedRoleId != null) "Continue to ${getRoleDisplayName(selectedRoleId!!)} Login" else "Select Role & Continue",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Already have an account? ",
                  fontSize = 14.sp,
                  color = Color(0xFF616161)
                )
                Text(
                  text = "Log In",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32),
                  modifier = Modifier.clickable {
                    val targetRole = selectedRoleId ?: "farmer"
                    Log.d("AgroWorldNav", "ROLE_SELECTED: $targetRole")
                    Log.d("AgroWorldNav", "OPENING_LOGIN: $targetRole")
                    navController.navigate("login?role=$targetRole")
                  }
                )
              }
            }
          }
        }
      },
      modifier = Modifier.testTag("role_selection_screen")
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFFF8FBF7))
          .padding(paddingValues),
        contentAlignment = Alignment.TopCenter
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 1200.dp)
            .padding(horizontal = if (isDesktop) 32.dp else 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 16.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "Welcome to AgroWorld",
              fontSize = if (isDesktop) 28.sp else 24.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF212121),
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Select your role to access your dedicated agricultural ecosystem",
              fontSize = 14.sp,
              color = Color(0xFF616161),
              textAlign = TextAlign.Center
            )
          }

          if (isDesktop || isTablet) {
            LazyVerticalGrid(
              columns = GridCells.Adaptive(minSize = 340.dp),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
              contentPadding = PaddingValues(bottom = 24.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(roles) { role ->
                val isSelected = selectedRoleId == role.id
                Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFFFFFF)
                  ),
                  border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF2E7D32)) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                  elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      selectedRoleId = role.id
                      Log.d("AgroWorldNav", "ROLE_SELECTED: ${role.id}")
                      Log.d("AgroWorldNav", "OPENING_LOGIN: ${role.id}")
                      navController.navigate("login?role=${role.id}")
                    }
                    .testTag("role_card_${role.id}")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color(0xFFF1F8E9)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = role.icon,
                        contentDescription = role.name,
                        tint = if (isSelected) Color.White else Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = role.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF212121)
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = role.description,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        lineHeight = 16.sp
                      )
                    }

                    if (isSelected) {
                      Spacer(modifier = Modifier.width(8.dp))
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Selected",
                          tint = Color.White,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(14.dp),
              contentPadding = PaddingValues(bottom = 24.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(roles) { role ->
                val isSelected = selectedRoleId == role.id
                Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFFFFFFF)
                  ),
                  border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF2E7D32)) else BorderStroke(1.dp, Color(0xFFE0E0E0)),
                  elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      selectedRoleId = role.id
                      Log.d("AgroWorldNav", "ROLE_SELECTED: ${role.id}")
                      Log.d("AgroWorldNav", "OPENING_LOGIN: ${role.id}")
                      navController.navigate("login?role=${role.id}")
                    }
                    .testTag("role_card_${role.id}")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color(0xFFF1F8E9)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = role.icon,
                        contentDescription = role.name,
                        tint = if (isSelected) Color.White else Color(0xFF2E7D32),
                        modifier = Modifier.size(26.dp)
                      )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = role.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF212121)
                      )
                      Spacer(modifier = Modifier.height(3.dp))
                      Text(
                        text = role.description,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        lineHeight = 16.sp
                      )
                    }

                    if (isSelected) {
                      Spacer(modifier = Modifier.width(8.dp))
                      Box(
                        modifier = Modifier
                          .size(24.dp)
                          .clip(CircleShape)
                          .background(Color(0xFF2E7D32)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = "Selected",
                          tint = Color.White,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

// ------------------ DYNAMIC DASHBOARD SCREEN ------------------
@Composable
fun DashboardScreen(role: String, navController: NavController) {
  var activeTab by remember { mutableStateOf(0) }
  val context = LocalContext.current

  Scaffold(
    topBar = {
      Surface(
        tonalElevation = 0.dp,
        color = Color(0xFFFDFBFF),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
              navController.navigate("role_selection") {
                popUpTo("role_selection") { inclusive = true }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back to roles",
              tint = Color(0xFF0061A4)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "AgroWorld Portal",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0061A4)
            )
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0xFFD6E2FF)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "JD",
              color = Color(0xFF001B3E),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = Color(0xFFF0F4F9),
        tonalElevation = 0.dp,
        modifier = Modifier.drawBehind {
          // Subtle top border matching bottom nav bar border-t border-[#DDE2EA]
          drawLine(
            color = Color(0xFFDDE2EA),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx()
          )
        }
      ) {
        NavigationBarItem(
          selected = activeTab == 0,
          onClick = { activeTab = 0 },
          icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Home", fontSize = 11.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF001D36),
            selectedTextColor = Color(0xFF001D36),
            unselectedIconColor = Color(0xFF44474E),
            unselectedTextColor = Color(0xFF44474E),
            indicatorColor = Color(0xFFD3E4FF)
          )
        )
        NavigationBarItem(
          selected = activeTab == 1,
          onClick = { activeTab = 1 },
          icon = { Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Analytics") },
          label = { Text("Insights", fontSize = 11.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF001D36),
            selectedTextColor = Color(0xFF001D36),
            unselectedIconColor = Color(0xFF44474E),
            unselectedTextColor = Color(0xFF44474E),
            indicatorColor = Color(0xFFD3E4FF)
          )
        )
        NavigationBarItem(
          selected = activeTab == 2,
          onClick = { activeTab = 2 },
          icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
          label = { Text("Settings", fontSize = 11.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal) },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF001D36),
            selectedTextColor = Color(0xFF001D36),
            unselectedIconColor = Color(0xFF44474E),
            unselectedTextColor = Color(0xFF44474E),
            indicatorColor = Color(0xFFD3E4FF)
          )
        )
      }
    },
    modifier = Modifier.testTag("dashboard_screen")
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFDFBFF))
        .padding(paddingValues)
    ) {
      if (activeTab == 0) {
        when (role.lowercase()) {
          "farmer" -> FarmerDashboardView()
          "customer" -> CustomerDashboardView()
          "broker" -> BrokerDashboardView()
          "seller" -> SellerDashboardView()
          "delivery" -> DeliveryDashboardView()
          else -> CustomerDashboardView()
        }
      } else if (activeTab == 1) {
        InsightsTabView(role = role)
      } else {
        SettingsTabView(role = role, navController = navController)
      }
    }
  }
}

// ------------------ FARMER DASHBOARD ------------------
@Composable
fun FarmerDashboardView() {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0FF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Welcome, Agro-Farmer!", color = Color(0xFF0061A4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text("My Harvest Portal", color = Color(0xFF001B3E), fontSize = 24.sp, fontWeight = FontWeight.Light)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Total Active Crop Listings: 4 Crops",
            color = Color(0xFF001B3E),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF0061A4))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Location: Maharashtra Region • Soil Index: Excellent",
              color = Color(0xFF0061A4),
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Crop Live Market Prices",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
      )
    }

    val crops = listOf(
      Triple("Premium Basmati Rice", "₹42,500 / Ton", "+4.2%"),
      Triple("Organic Durum Wheat", "₹26,800 / Ton", "+1.8%"),
      Triple("High-Grade Soybean", "₹38,200 / Ton", "-0.5%"),
      Triple("White Sweet Corn", "₹18,400 / Ton", "+5.1%")
    )

    items(crops) { crop ->
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF2F0F4)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "Crop",
                tint = Color(0xFF0061A4)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(crop.first, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
              Text("AgroWorld Verified", fontSize = 12.sp, color = Color(0xFF44474E))
            }
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(crop.second, fontWeight = FontWeight.Bold, color = Color(0xFF0061A4))
            Text(
              text = crop.third,
              fontSize = 12.sp,
              color = if (crop.third.startsWith("+")) Color(0xFF0061A4) else Color(0xFFBA1A1A),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
      Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add listing")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Create New Crop Listing", fontWeight = FontWeight.Bold)
      }
    }
  }
}

// ------------------ CUSTOMER DASHBOARD ------------------
@Composable
fun CustomerDashboardView() {
  var cartCount by remember { mutableStateOf(0) }
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0FF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Fresh Farm-Direct Produce", color = Color(0xFF0061A4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Healthy Organic Table", color = Color(0xFF001B3E), fontSize = 22.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Cart Items: $cartCount selected", color = Color(0xFF001B3E), fontSize = 12.sp, fontWeight = FontWeight.Medium)
          }
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(Color(0xFFD3E4FF))
              .clickable {
                Toast
                  .makeText(context, "Proceeding to checkout with $cartCount items!", Toast.LENGTH_SHORT)
                  .show()
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingBasket,
              contentDescription = "Cart",
              tint = Color(0xFF001B3E),
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Organic Field Offerings",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1A1C1E)
        )
        Text(
          text = "View All",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0061A4),
          modifier = Modifier.clickable {}
        )
      }
    }

    val products = listOf(
      Triple("Organic Red Apples", "₹180 / Kg", "Freshly Picked Himachal Orchard"),
      Triple("Farm-Fresh Sweet Corn", "₹60 / Bunch", "Harvested Today from Pune"),
      Triple("Premium Basmati Grains", "₹120 / Kg", "Dehradun Special Aged 1 Year"),
      Triple("Pure Raw Forest Honey", "₹380 / Bottle", "Natures Pure Untampered")
    )

    items(products) { prod ->
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(prod.first, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Text(prod.third, fontSize = 12.sp, color = Color(0xFF44474E))
            Spacer(modifier = Modifier.height(4.dp))
            Text(prod.second, fontWeight = FontWeight.Bold, color = Color(0xFF0061A4))
          }

          Button(
            onClick = {
              cartCount++
              Toast.makeText(context, "Added ${prod.first} to cart!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFFE7F0FF),
              contentColor = Color(0xFF0061A4)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add",
              tint = Color(0xFF0061A4),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add", color = Color(0xFF0061A4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// ------------------ BROKER DASHBOARD ------------------
@Composable
fun BrokerDashboardView() {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Distribution Hub Terminal", color = Color(0xFFFF8D4D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Wholesale Exchange Platform", color = Color(0xFF1A1C1E), fontSize = 22.sp, fontWeight = FontWeight.Light)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Pending Wholesale Contracts: 3 Agreements", color = Color(0xFF1A1C1E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
      }
    }

    item {
      Text(
        text = "Active High-Volume Trades",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
      )
    }

    val trades = listOf(
      Triple("Punjab Grains -> Mumbai Wholesalers", "Wheat 15 Tons", "Awaiting Broker Escrow"),
      Triple("Nashik Farms -> Delhi Supermarkets", "Onions 8 Tons", "In Route Logistics"),
      Triple("Shimla Orchard Co -> South Fruits Corp", "Apples 12 Tons", "Authorized & Cleared")
    )

    items(trades) { trade ->
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(trade.first, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFF1E8))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "Broker Safe",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8D4D)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(trade.second, fontWeight = FontWeight.Medium, color = Color(0xFF0061A4), fontSize = 14.sp)
          Text(trade.third, fontSize = 12.sp, color = Color(0xFF44474E))
        }
      }
    }
  }
}

// ------------------ SELLER DASHBOARD ------------------
@Composable
fun SellerDashboardView() {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0FF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Agro-Merchant Center", color = Color(0xFF0061A4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Inventory & Supply Dashboard", color = Color(0xFF001B3E), fontSize = 22.sp, fontWeight = FontWeight.Light)
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Active Stocks: 140 Packages", color = Color(0xFF001B3E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("Revenue: ₹2,45,000", color = Color(0xFF0061A4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text(
        text = "Farming Supplies Catalog",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
      )
    }

    val inventory = listOf(
      Triple("High-Yield Paddy Seeds", "45 Boxes in Stock", "₹1,200 / Box"),
      Triple("Organic Potassium Fertilizer", "80 Bags in Stock", "₹850 / Bag"),
      Triple("Premium Agricultural Drip Pipes", "12 Roll Reels left", "₹4,500 / Reel"),
      Triple("Manual Seed Sowing Tool", "6 Units remaining", "₹2,200 / Unit")
    )

    items(inventory) { item ->
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(item.first, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Text(item.second, fontSize = 12.sp, color = Color(0xFF44474E))
          }
          Text(item.third, fontWeight = FontWeight.Bold, color = Color(0xFF0061A4))
        }
      }
    }
  }
}

// ------------------ DELIVERY PARTNER DASHBOARD ------------------
@Composable
fun DeliveryDashboardView() {
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F0F4)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(24.dp)) {
          Text("Logistics Route Command", color = Color(0xFF0061A4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Express Farm Freight Network", color = Color(0xFF1A1C1E), fontSize = 22.sp, fontWeight = FontWeight.Light)
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Available Deliveries: 4 Nearby", color = Color(0xFF1A1C1E), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("Completed Today: 3 Jobs", color = Color(0xFF0061A4), fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text(
        text = "Available Pickups Nearby",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
      )
    }

    val jobs = listOf(
      Triple("Nashik Farm -> Mumbai Cold Storage", "Grapes 400 Kg • ₹3,200 payout", "Distance: 120 Km"),
      Triple("Green-Orchard -> Local Retail Market", "Apples 180 Kg • ₹1,200 payout", "Distance: 24 Km"),
      Triple("Soil-Bio Warehouse -> Agro-Coop Center", "Seed Bags 1.2 Tons • ₹4,500 payout", "Distance: 85 Km")
    )

    items(jobs) { job ->
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(job.first, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Text(job.third, fontSize = 12.sp, color = Color(0xFF44474E))
            Spacer(modifier = Modifier.height(4.dp))
            Text(job.second, fontWeight = FontWeight.Bold, color = Color(0xFF0061A4), fontSize = 14.sp)
          }

          Button(
            onClick = {
              Toast.makeText(context, "Delivery Job Accepted! Navigate to location.", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.height(36.dp)
          ) {
            Text("Accept", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// ------------------ TAB COMPONENT: INSIGHTS ------------------
@Composable
fun InsightsTabView(role: String) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Text(
      text = "${role.replaceFirstChar { it.uppercase() }} Industry Analytics",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = Color(0xFF1A1C1E)
    )

    Text(
      text = "AgroWorld provides comprehensive, real-time market insights utilizing historical field datasets.",
      fontSize = 14.sp,
      color = Color(0xFF44474E)
    )

    Card(
      shape = RoundedCornerShape(30.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F0FF)),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.TrendingUp,
          contentDescription = "Trending",
          tint = Color(0xFF0061A4),
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Market Outlook: Highly Bullish",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF001B3E)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Agricultural crop demands in urban regions are expected to increase by 14% over the coming weeks due to seasonal changes. Harvest planning is strongly recommended.",
          fontSize = 13.sp,
          color = Color(0xFF44474E),
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

// ------------------ TAB COMPONENT: SETTINGS ------------------
@Composable
fun SettingsTabView(role: String, navController: NavController) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(20.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Account settings",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1A1C1E)
      )

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Account Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
          Spacer(modifier = Modifier.height(4.dp))
          Text("Logged in role: ${role.replaceFirstChar { it.uppercase() }}", fontSize = 14.sp, color = Color(0xFF44474E))
        }
      }

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFC4C6D0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1C1E))
          Spacer(modifier = Modifier.height(4.dp))
          Text("Push messages & crop alert signals enabled", fontSize = 14.sp, color = Color(0xFF44474E))
        }
      }
    }

    Button(
      onClick = {
        navController.navigate("role_selection") {
          popUpTo("role_selection") { inclusive = true }
        }
      },
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
    ) {
      Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.White)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Exit Portal", fontWeight = FontWeight.Bold, color = Color.White)
    }
  }
}
