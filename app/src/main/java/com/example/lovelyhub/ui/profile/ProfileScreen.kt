package com.example.lovelyhub.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.repository.ListingRepository
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.listings.ListingViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    listingViewModel: ListingViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listingRepository = remember { ListingRepository() }

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val userListings by listingViewModel.userListings.collectAsState()
    val currentUser = viewModel.currentUser

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }
    var businessName by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var businessLocation by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var isEditFormExpanded by remember { mutableStateOf(false) }
    var showMyListingsDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var listingToDelete by remember { mutableStateOf<Listing?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newPhotoUri = uri
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF1E1E2D),
        unfocusedTextColor = Color(0xFF1E1E2D),
        focusedLabelColor = Color(0xFF3F51B5),
        unfocusedLabelColor = Color(0xFF5C6BC0),
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLeadingIconColor = Color(0xFF3F51B5),
        unfocusedLeadingIconColor = Color(0xFF3F51B5),
        focusedBorderColor = Color(0xFF3F51B5),
        unfocusedBorderColor = Color.LightGray,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    val topHeaderGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0),
            Color(0xFFE8EAF6)
        )
    )

    LaunchedEffect(Unit) {
        listingViewModel.fetchUserListings()
    }

    LaunchedEffect(userProfile, currentUser) {
        if (userProfile != null) {
            val profile = userProfile!!
            name = profile.name
            email = profile.email
            role = profile.role
            businessName = profile.businessName
            businessPhone = profile.businessPhone
            businessLocation = profile.businessLocation
            photoUrl = profile.photoUrl
        } else if (currentUser != null) {
            name = currentUser.displayName ?: ""
            email = currentUser.email ?: ""
            photoUrl = currentUser.photoUrl?.toString() ?: ""
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F9))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(topHeaderGradient)
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MY PROFILE & ACCOUNT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (newPhotoUri != null) {
                                AsyncImage(
                                    model = newPhotoUri,
                                    contentDescription = "New Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFD54F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color(0xFF1E1E2D),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = name.ifBlank { "Campus User" },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = role,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMyListingsDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8EAF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = Color(0xFF3F51B5),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "My Uploaded Listings",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Text(
                                        text = "${userListings.size} active listings added by you",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF8E1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Push Notifications",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Text(
                                        text = if (isNotificationsEnabled) "Enabled for messages & orders" else "Disabled",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Switch(
                                checked = isNotificationsEnabled,
                                onCheckedChange = {
                                    isNotificationsEnabled = it
                                    scope.launch {
                                        snackbarHostState.showSnackbar(if (it) "Notifications Enabled" else "Notifications Disabled")
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF3F51B5)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHelpSupportDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Help & Support",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Text(
                                        text = "24/7 Helpline, WhatsApp & FAQs",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditFormExpanded = !isEditFormExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Edit Profile & Business Info",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Text(
                                        text = "Update name, phone & campus location",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isEditFormExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Full Name", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { Text("Email Address", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = role,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Account Role", fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                        singleLine = true,
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    if (role != "Student") {
                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = businessName,
                                            onValueChange = { businessName = it },
                                            label = { Text("Business / Shop Name", fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                            singleLine = true,
                                            colors = fieldColors,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = businessPhone,
                                            onValueChange = { businessPhone = it },
                                            label = { Text("Business Phone", fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            colors = fieldColors,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        OutlinedTextField(
                                            value = businessLocation,
                                            onValueChange = { businessLocation = it },
                                            label = { Text("Campus Location", fontWeight = FontWeight.Bold) },
                                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3F51B5)) },
                                            singleLine = true,
                                            colors = fieldColors,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = {
                                            isSaving = true
                                            scope.launch {
                                                val finalPhoto = if (newPhotoUri != null) {
                                                    listingRepository.compressAndEncodeImage(context, newPhotoUri!!)
                                                } else {
                                                    photoUrl
                                                }

                                                viewModel.updateUserProfile(
                                                    name = name,
                                                    email = email,
                                                    role = role,
                                                    businessName = businessName,
                                                    businessPhone = businessPhone,
                                                    businessLocation = businessLocation,
                                                    photoUrl = finalPhoto
                                                ) { error ->
                                                    isSaving = false
                                                    scope.launch {
                                                        if (error == null) {
                                                            snackbarHostState.showSnackbar("Profile updated successfully!")
                                                        } else {
                                                            snackbarHostState.showSnackbar(error)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        enabled = !isSaving,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                    ) {
                                        if (isSaving) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                                        } else {
                                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFD32F2F))
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

    if (showHelpSupportDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSupportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Campus Help & Support", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Need help with your orders, rooms, or listings? Contact our campus developer support team:",
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9086455406"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Support: 9086455406", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val waUri = Uri.parse("https:" + "/" + "/api.whatsapp.com/send?phone=919086455406&text=" + Uri.encode("Hello! I need help with LovelyHub app."))
                            val intent = Intent(Intent.ACTION_VIEW, waUri)
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat on WhatsApp 📲", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:lonekaisar15@gmail.com"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8EAF6), contentColor = Color(0xFF3F51B5))
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email: lonekaisar15@gmail.com", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpSupportDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                }
            }
        )
    }

    if (showMyListingsDialog) {
        AlertDialog(
            onDismissRequest = { showMyListingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("My Uploaded Listings", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            },
            text = {
                if (userListings.isEmpty()) {
                    Text("You haven't uploaded any listings or services yet.", fontSize = 14.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(userListings) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E1E2D))
                                        Text("${item.category} • ${item.price}", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    IconButton(
                                        onClick = { listingToDelete = item },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showMyListingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Delete Listing", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${listingToDelete?.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        val target = listingToDelete
                        listingToDelete = null
                        if (target != null) {
                            listingViewModel.deleteListing(target.id, target.category) {
                                scope.launch { snackbarHostState.showSnackbar("Listing deleted!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
