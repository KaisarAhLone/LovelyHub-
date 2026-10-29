package com.example.lovelyhub.ui.profile

import android.Manifest
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.data.local.LocalUserCache
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.repository.ListingRepository
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.components.MapLocationHelper
import com.example.lovelyhub.ui.listings.ListingViewModel
import com.example.lovelyhub.ui.theme.LovelyAmber
import com.example.lovelyhub.ui.theme.LovelyDarkPinkAccent
import com.example.lovelyhub.ui.theme.LovelyDarkPinkGradient
import com.example.lovelyhub.ui.theme.LovelyDarkPinkSoft
import com.example.lovelyhub.ui.theme.LovelyHeaderDarkPinkGradient
import com.example.lovelyhub.ui.theme.LovelyPurplePrimary
import com.example.lovelyhub.ui.theme.LovelyPurpleSoft
import com.example.lovelyhub.ui.theme.LovelyTextDark
import com.example.lovelyhub.ui.theme.LovelyTextMuted
import com.google.firebase.auth.FirebaseAuth
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

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val userListings by listingViewModel.userListings.collectAsState()
    val currentUser = viewModel.currentUser

    val localUserCache = remember { LocalUserCache(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }
    var businessName by remember { mutableStateOf("") }
    var businessPhone by remember { mutableStateOf("") }
    var businessLocation by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isFetchingGps by remember { mutableStateOf(false) }

    var isEditFormExpanded by remember { mutableStateOf(false) }
    var showMyListingsDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var listingToDelete by remember { mutableStateOf<Listing?>(null) }

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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newPhotoUri = uri
            scope.launch {
                val uid = currentUser?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val localFilePath = localUserCache.saveProfilePhotoFile(uid, uri)
                val encodedPhoto = listingViewModel.compressImageSync(context, uri)
                if (encodedPhoto.isNotBlank()) {
                    localUserCache.saveProfilePhotoBase64(uid, encodedPhoto)
                }
                photoUrl = if (localFilePath.isNotBlank()) localFilePath else encodedPhoto
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LovelyTextDark,
        unfocusedTextColor = LovelyTextDark,
        focusedLabelColor = LovelyPurplePrimary,
        unfocusedLabelColor = LovelyPurpleSoft,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLeadingIconColor = LovelyPurplePrimary,
        unfocusedLeadingIconColor = LovelyPurplePrimary,
        focusedBorderColor = LovelyPurplePrimary,
        unfocusedBorderColor = Color.LightGray,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    LaunchedEffect(Unit) {
        listingViewModel.fetchUserListings()
    }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            viewModel.loadUserProfile(uid, context)
        }
    }

    LaunchedEffect(userProfile, currentUser) {
        val uid = currentUser?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val cachedPhoto = localUserCache.getProfilePhoto(uid)

        if (userProfile != null) {
            val profile = userProfile!!
            name = profile.name
            email = profile.email
            role = profile.role
            businessName = profile.businessName
            businessPhone = profile.businessPhone
            businessLocation = profile.businessLocation
            val finalPhoto = profile.photoUrl.ifBlank { cachedPhoto ?: "" }
            if (photoUrl.isBlank() || (finalPhoto.isNotBlank() && photoUrl != finalPhoto && newPhotoUri == null)) {
                photoUrl = finalPhoto
            }
        } else {
            if (currentUser != null) {
                name = currentUser.displayName ?: ""
                email = currentUser.email ?: ""
            }
            if (!cachedPhoto.isNullOrBlank() && newPhotoUri == null) {
                photoUrl = cachedPhoto
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LovelyDarkPinkGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(LovelyHeaderDarkPinkGradient)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 28.dp),
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
                                    .border(3.dp, LovelyAmber, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (newPhotoUri != null) {
                                    AsyncImage(
                                        model = newPhotoUri,
                                        contentDescription = "Profile Photo",
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
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = name.ifBlank { "Campus User" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
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
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Text(
                            text = "Manage Listings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LovelyDarkPinkSoft
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMyListingsDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                            .background(LovelyPurpleSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory,
                                            contentDescription = null,
                                            tint = LovelyPurplePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    val activeItemsList = remember(userListings, currentUser) {
                                        val uid = currentUser?.uid ?: ""
                                        userListings.filter { !it.isShop && it.id != uid }
                                    }

                                    Column {
                                        Text(
                                            text = "My Uploaded Listings",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = LovelyTextDark
                                        )
                                        Text(
                                            text = "${activeItemsList.size} active items added by you",
                                            fontSize = 12.sp,
                                            color = LovelyTextMuted
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = LovelyTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Account & Preferences",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = LovelyDarkPinkSoft
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isEditFormExpanded = !isEditFormExpanded }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(LovelyDarkPinkSoft),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = LovelyDarkPinkAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = "Edit Profile & Business Info",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = LovelyTextDark
                                            )
                                            Text(
                                                text = "Update name, phone & campus location",
                                                fontSize = 12.sp,
                                                color = LovelyTextMuted
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = LovelyTextMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                AnimatedVisibility(visible = isEditFormExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 16.dp)
                                    ) {
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = "Profile & Business Photo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LovelyTextDark
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFFCE4EC))
                                                .clickable { imagePickerLauncher.launch("image/*") }
                                                .padding(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .border(2.dp, LovelyAmber, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (newPhotoUri != null) {
                                                    AsyncImage(
                                                        model = newPhotoUri,
                                                        contentDescription = "New Photo",
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
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = "Change Profile & Shop Photo",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = LovelyDarkPinkAccent
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Updates profile & shop card photo",
                                                    fontSize = 11.sp,
                                                    color = LovelyTextMuted
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        OutlinedTextField(
                                            value = name,
                                            onValueChange = { name = it },
                                            label = { Text("Full Name") },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                            singleLine = true,
                                            colors = fieldColors,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Email Address (Locked)") },
                                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                                            trailingIcon = { Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                                            singleLine = true,
                                            colors = fieldColors.copy(
                                                disabledContainerColor = Color(0xFFF3F4F6),
                                                unfocusedContainerColor = Color(0xFFF9FAFB)
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        if (role != "Student") {
                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = businessName,
                                                onValueChange = { businessName = it },
                                                label = { Text("Business / Shop Name") },
                                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                                                singleLine = true,
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = businessPhone,
                                                onValueChange = { businessPhone = it },
                                                label = { Text("Business Contact Phone") },
                                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                singleLine = true,
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            OutlinedTextField(
                                                value = businessLocation,
                                                onValueChange = { businessLocation = it },
                                                label = { Text("Campus Location / Address") },
                                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                                singleLine = true,
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { MapLocationHelper.openMapPicker(context, businessLocation) },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Icon(Icons.Default.Map, contentDescription = null, tint = LovelyDarkPinkAccent, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Select on Map", fontSize = 12.sp, color = LovelyDarkPinkAccent, fontWeight = FontWeight.SemiBold)
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
                                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = LovelyDarkPinkAccent)
                                                    } else {
                                                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = LovelyDarkPinkAccent, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Current GPS", fontSize = 12.sp, color = LovelyDarkPinkAccent, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                if (name.isBlank()) {
                                                    scope.launch { snackbarHostState.showSnackbar("Name cannot be empty") }
                                                    return@Button
                                                }

                                                isSaving = true
                                                viewModel.updateUserProfile(
                                                    context = context,
                                                    name = name,
                                                    email = email,
                                                    role = role,
                                                    businessName = businessName,
                                                    businessPhone = businessPhone,
                                                    businessLocation = businessLocation,
                                                    photoUrl = photoUrl
                                                ) { error ->
                                                    isSaving = false
                                                    if (error == null) {
                                                        isEditFormExpanded = false
                                                        scope.launch { snackbarHostState.showSnackbar("Profile updated successfully!") }
                                                    } else {
                                                        scope.launch { snackbarHostState.showSnackbar(error) }
                                                    }
                                                }
                                            },
                                            enabled = !isSaving,
                                            colors = ButtonDefaults.buttonColors(containerColor = LovelyDarkPinkAccent),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (isSaving) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                            } else {
                                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Save Profile Changes", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(LovelyAmber.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = LovelyAmber,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "Push Notifications",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = LovelyTextDark
                                        )
                                        Text(
                                            text = "Enabled for messages & orders",
                                            fontSize = 12.sp,
                                            color = LovelyTextMuted
                                        )
                                    }
                                }

                                Switch(
                                    checked = isNotificationsEnabled,
                                    onCheckedChange = { isNotificationsEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = LovelyDarkPinkAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHelpSupportDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                            .background(LovelyPurpleSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = null,
                                            tint = LovelyPurplePrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "Help & Support",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = LovelyTextDark
                                        )
                                        Text(
                                            text = "24/7 Helpline, WhatsApp & FAQs",
                                            fontSize = 12.sp,
                                            color = LovelyTextMuted
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = LovelyTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log Out")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out of Account", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    if (showMyListingsDialog) {
        val activeItemsList = remember(userListings, currentUser) {
            val uid = currentUser?.uid ?: ""
            userListings.filter { !it.isShop && it.id != uid }
        }

        AlertDialog(
            onDismissRequest = { showMyListingsDialog = false },
            title = {
                Column {
                    Text("Your Active Items", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LovelyTextDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Items, dishes, services & jobs added by you", fontSize = 11.sp, color = LovelyTextMuted)
                }
            },
            text = {
                if (activeItemsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("You haven't added any items or dishes yet.", color = LovelyTextMuted, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(activeItemsList) { item ->
                            val categoryEmoji = when {
                                item.category.contains("Food", ignoreCase = true) || item.category.contains("Restaurant", ignoreCase = true) -> "🍔 "
                                item.category.contains("Service", ignoreCase = true) -> "🛠️ "
                                item.category.contains("Job", ignoreCase = true) -> "💼 "
                                item.category.contains("Room", ignoreCase = true) -> "🛏️ "
                                item.category.contains("Rental", ignoreCase = true) -> "🚗 "
                                else -> "🛍️ "
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "$categoryEmoji${item.title}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = LovelyTextDark
                                        )

                                        Spacer(modifier = Modifier.height(3.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(LovelyPurpleSoft)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = item.category,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LovelyPurplePrimary
                                                )
                                            }

                                            if (item.price.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = item.price,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = LovelyDarkPinkAccent
                                                )
                                            }
                                        }

                                        if (item.menuItems.isNotEmpty()) {
                                            val firstMenuItem = item.menuItems.firstOrNull() ?: ""
                                            val parts = firstMenuItem.split("|")
                                            val desc = parts.getOrNull(2)?.trim() ?: parts.getOrNull(1)?.trim() ?: ""
                                            if (desc.isNotBlank() && !desc.contains("data:image") && !desc.contains("http")) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = desc,
                                                    fontSize = 11.sp,
                                                    color = LovelyTextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { listingToDelete = item },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMyListingsDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = LovelyPurplePrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showHelpSupportDialog) {
        AlertDialog(
            onDismissRequest = { showHelpSupportDialog = false },
            title = {
                Text("Help & Support", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LovelyTextDark)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📞 Support Helpline: 9086455406", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LovelyTextDark)
                    Text("✉️ Email: lonekaisar15@gmail.com", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LovelyTextDark)
                    Text("💬 Available 24/7 for students, hostelers & campus sellers.", fontSize = 12.sp, color = LovelyTextMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showHelpSupportDialog = false
                        val waUri = Uri.parse("https:" + "/" + "/wa.me/919086455406?text=" + Uri.encode("Hello! I need help with LovelyHub app."))
                        try { context.startActivity(Intent(Intent.ACTION_VIEW, waUri)) } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Chat on WhatsApp", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpSupportDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
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
                        val item = listingToDelete
                        listingToDelete = null
                        if (item != null) {
                            listingViewModel.deleteListing(item.id, item.category) {
                                scope.launch { snackbarHostState.showSnackbar("Listing deleted!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text("Log Out", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LovelyTextDark)
            },
            text = {
                Text("Are you sure you want to log out of your account?", fontSize = 14.sp, color = LovelyTextMuted)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
