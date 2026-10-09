package com.example.lovelyhub.ui.auth

import android.Manifest
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lovelyhub.R
import com.example.lovelyhub.data.model.UserRole
import com.example.lovelyhub.ui.components.GoogleSignInButton
import com.example.lovelyhub.ui.components.MapLocationHelper
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var isRoleDropdownExpanded by remember { mutableStateOf(false) }

    var businessName by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var businessWhatsApp by remember { mutableStateOf("") }
    var businessLocation by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("09:00 AM") }
    var closeTime by remember { mutableStateOf("10:00 PM") }
    var is24Hours by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var isFetchingGps by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val webClientId = stringResource(R.string.default_web_client_id)

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFFFFB300)
        )
    )

    fun showClockPicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        val picker = TimePickerDialog(
            context,
            { _, h, m ->
                val isAm = h < 12
                val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
                val formatted = String.format(Locale.getDefault(), "%02d:%02d %s", h12, m, if (isAm) "AM" else "PM")
                onTimeSelected(formatted)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )
        picker.show()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            isFetchingGps = true
            MapLocationHelper.getCurrentGpsAddress(context) { gpsLoc ->
                isFetchingGps = false
                businessLocation = gpsLoc
                scope.launch { snackbarHostState.showSnackbar("GPS Location detected!") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Location permission required") }
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onAuthSuccess()
            }
            is AuthState.Error -> {
                val msg = (authState as AuthState.Error).message
                snackbarHostState.showSnackbar(msg)
                viewModel.clearError()
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorfulGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LH",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "LOVELY HUB",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )

                        Text(
                            text = "'Everything Around Your Campus'",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color(0xFFF0E5FC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .clip(RoundedCornerShape(12.dp)),
                                    color = Color(0xFF673AB7)
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                text = {
                                    Text(
                                        text = "Login",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (selectedTabIndex == 0) Color(0xFF673AB7) else Color.Gray
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                text = {
                                    Text(
                                        text = "Register",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (selectedTabIndex == 1) Color(0xFF673AB7) else Color.Gray
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        AnimatedVisibility(visible = selectedTabIndex == 1) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF0E5FC))
                                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .clickable { imagePickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profileImageUri != null) {
                                        AsyncImage(
                                            model = profileImageUri,
                                            contentDescription = "Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                tint = Color(0xFF673AB7),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Upload Profile / Cover Photo",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF673AB7)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Full Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF673AB7)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedRole.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Account Type / Role") },
                                        trailingIcon = {
                                            IconButton(onClick = { isRoleDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF673AB7))
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    DropdownMenu(
                                        expanded = isRoleDropdownExpanded,
                                        onDismissRequest = { isRoleDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.85f)
                                    ) {
                                        UserRole.entries.forEach { role ->
                                            DropdownMenuItem(
                                                text = { Text(role.displayName, fontSize = 14.sp) },
                                                onClick = {
                                                    selectedRole = role
                                                    isRoleDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                if (selectedRole != UserRole.STUDENT) {
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = businessName,
                                        onValueChange = { businessName = it },
                                        label = { Text("Shop / Hotel / Business Name") },
                                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = businessPhone,
                                        onValueChange = { businessPhone = it },
                                        label = { Text("Business Calling Phone Number") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = businessWhatsApp,
                                        onValueChange = { businessWhatsApp = it },
                                        label = { Text("WhatsApp Number for Orders (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2E7D32)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = businessLocation,
                                            onValueChange = { businessLocation = it },
                                            label = { Text("Campus Location") },
                                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF673AB7)) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    MapLocationHelper.openMapPicker(context, businessLocation)
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Select on Map", fontSize = 12.sp, color = Color(0xFF2196F3), fontWeight = FontWeight.SemiBold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                if (isFetchingGps) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF673AB7))
                                                } else {
                                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Current GPS", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = is24Hours,
                                            onCheckedChange = { is24Hours = it },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                                        )
                                        Text("Open 24 Hours", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }

                                    if (!is24Hours) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = openTime,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Open Time") },
                                                    leadingIcon = {
                                                        IconButton(onClick = { showClockPicker { openTime = it } }) {
                                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF673AB7))
                                                        }
                                                    },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        showClockPicker { openTime = it }
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }

                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = closeTime,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Close Time") },
                                                    leadingIcon = {
                                                        IconButton(onClick = { showClockPicker { closeTime = it } }) {
                                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF673AB7))
                                                        }
                                                    },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth().clickable {
                                                        showClockPicker { closeTime = it }
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF673AB7)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF673AB7)) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (selectedTabIndex == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(onClick = { showForgotPasswordDialog = true }) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontSize = 12.sp,
                                        color = Color(0xFF673AB7),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (selectedTabIndex == 1) {
                                    viewModel.signUpWithEmail(
                                        context = context,
                                        name = name,
                                        email = email,
                                        password = password,
                                        role = selectedRole.displayName,
                                        businessName = businessName,
                                        businessPhone = businessPhone,
                                        businessWhatsApp = businessWhatsApp,
                                        businessLocation = businessLocation,
                                        openTime = openTime,
                                        closeTime = closeTime,
                                        is24Hours = is24Hours,
                                        imageUri = profileImageUri
                                    )
                                } else {
                                    viewModel.signInWithEmail(email, password)
                                }
                            },
                            enabled = authState !is AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                        ) {
                            if (authState is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedTabIndex == 1) "Register" else "Login",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "  OR  ",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color.Gray.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        GoogleSignInButton(
                            text = "Continue with Google",
                            onClick = {
                                viewModel.signInWithGoogle(context, webClientId)
                            }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your email address to receive a password reset link.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPassword(resetEmail) {
                            showForgotPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
