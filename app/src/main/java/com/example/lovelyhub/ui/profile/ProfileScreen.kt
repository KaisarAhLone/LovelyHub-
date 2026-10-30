package com.example.lovelyhub.ui.profile

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import java.util.Calendar
import java.util.Locale
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.onboarding.OnboardingScreen
import androidx.compose.material.icons.filled.AutoAwesome
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

data class MyListingDisplayItem(
    val id: String,
    val category: String,
    val title: String,
    val price: String,
    val imageUrl: String,
    val rawListing: Listing,
    val menuItemIndex: Int = -1
)

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
    var openTime by remember { mutableStateOf("09:00 AM") }
    var closeTime by remember { mutableStateOf("10:00 PM") }
    var is24Hours by remember { mutableStateOf(false) }
    var showClockPickerFor by remember { mutableStateOf<String?>(null) }
    var photoUrl by remember { mutableStateOf("") }
    var newPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isFetchingGps by remember { mutableStateOf(false) }

    var isEditFormExpanded by remember { mutableStateOf(false) }
    var showMyListingsDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MyListingDisplayItem?>(null) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var listingToDelete by remember { mutableStateOf<Listing?>(null) }
    var showOnboardingModal by remember { mutableStateOf(false) }

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
            openTime = profile.openTime.ifBlank { "09:00 AM" }
            closeTime = profile.closeTime.ifBlank { "10:00 PM" }
            is24Hours = profile.is24Hours
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
                                    ListingImage(
                                        imageUrl = photoUrl,
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

                                    val allDisplayItemsCount = remember(userListings, currentUser) {
                                        val uid = currentUser?.uid ?: ""
                                        var count = 0
                                        for (listing in userListings) {
                                            if (listing.isShop || listing.id == uid) {
                                                count += listing.menuItems.size
                                            } else {
                                                count += 1
                                            }
                                        }
                                        count
                                    }

                                    Column {
                                        Text(
                                            text = "My Uploaded Listings",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = LovelyTextDark
                                        )
                                        Text(
                                            text = "$allDisplayItemsCount active items added by you",
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
                                                    ListingImage(
                                                        imageUrl = photoUrl,
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

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("Open 24 Hours", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = LovelyTextDark)
                                                Switch(
                                                    checked = is24Hours,
                                                    onCheckedChange = { is24Hours = it },
                                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = LovelyDarkPinkAccent)
                                                )
                                            }

                                            if (!is24Hours) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedTextField(
                                                        value = openTime,
                                                        onValueChange = { openTime = it },
                                                        label = { Text("Open Time") },
                                                        singleLine = true,
                                                        colors = fieldColors,
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        trailingIcon = {
                                                            IconButton(onClick = { showClockPickerFor = "open" }) {
                                                                Icon(Icons.Default.AccessTime, contentDescription = "Pick Open Time", tint = LovelyPurplePrimary)
                                                            }
                                                        }
                                                    )

                                                    OutlinedTextField(
                                                        value = closeTime,
                                                        onValueChange = { closeTime = it },
                                                        label = { Text("Close Time") },
                                                        singleLine = true,
                                                        colors = fieldColors,
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        trailingIcon = {
                                                            IconButton(onClick = { showClockPickerFor = "close" }) {
                                                                Icon(Icons.Default.AccessTime, contentDescription = "Pick Close Time", tint = LovelyPurplePrimary)
                                                            }
                                                        }
                                                    )
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
                                                    photoUrl = photoUrl,
                                                    openTime = openTime,
                                                    closeTime = closeTime,
                                                    is24Hours = is24Hours
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

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFD32F2F)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Account Permanently", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    if (showMyListingsDialog) {
        val allDisplayItems = remember(userListings, currentUser) {
            val uid = currentUser?.uid ?: ""
            val result = mutableListOf<MyListingDisplayItem>()

            for (listing in userListings) {
                if (listing.isShop || listing.id == uid) {
                    listing.menuItems.forEachIndexed { idx, menuItemStr ->
                        val parts = menuItemStr.split("|").map { it.trim() }
                        val firstPart = parts.getOrNull(0) ?: "Item"

                        var title = firstPart
                        var price = ""
                        var imageUrl = ""

                        if (firstPart.contains(" - ")) {
                            val dashParts = firstPart.split(" - ")
                            title = dashParts[0].trim()
                            val p = dashParts.getOrNull(1)?.trim() ?: ""
                            if (p.startsWith("₹") || p.contains("/") || p.matches(Regex("^[0-9.]+$"))) {
                                price = if (p.startsWith("₹")) p else "₹$p"
                            }
                        }

                        for (i in 1 until parts.size) {
                            val p = parts[i]
                            if (price.isBlank() && (p.startsWith("₹") || p.contains("/day") || p.contains("/month") || p.matches(Regex("^[0-9.]+$")))) {
                                price = if (p.startsWith("₹")) p else "₹$p"
                            } else if (p.contains("data:image") || p.contains("http") || p.contains("content://") || p.contains("file:/")) {
                                if (imageUrl.isBlank()) imageUrl = p
                            }
                        }

                        if (imageUrl.isBlank()) {
                            imageUrl = listing.imageUrl.split("~").firstOrNull { it.isNotBlank() } ?: listing.imageUrl
                        }

                        val itemCat = when {
                            listing.category.contains("Restaurant", ignoreCase = true) || listing.category.contains("Food", ignoreCase = true) -> "Food"
                            listing.category.contains("Rental", ignoreCase = true) -> "Rentals"
                            listing.category.contains("Room", ignoreCase = true) -> "Rooms"
                            listing.category.contains("Service", ignoreCase = true) -> "Services"
                            listing.category.contains("Job", ignoreCase = true) -> "Jobs"
                            else -> listing.category
                        }

                        result.add(
                            MyListingDisplayItem(
                                id = listing.id,
                                category = itemCat,
                                title = title,
                                price = price.ifBlank { listing.price },
                                imageUrl = imageUrl,
                                rawListing = listing,
                                menuItemIndex = idx
                            )
                        )
                    }
                } else {
                    val itemCat = when {
                        listing.category.contains("Restaurant", ignoreCase = true) || listing.category.contains("Food", ignoreCase = true) -> "Food"
                        listing.category.contains("Rental", ignoreCase = true) -> "Rentals"
                        listing.category.contains("Room", ignoreCase = true) -> "Rooms"
                        listing.category.contains("Service", ignoreCase = true) -> "Services"
                        listing.category.contains("Job", ignoreCase = true) -> "Jobs"
                        else -> listing.category
                    }

                    result.add(
                        MyListingDisplayItem(
                            id = listing.id,
                            category = itemCat,
                            title = listing.title,
                            price = listing.price,
                            imageUrl = listing.imageUrl,
                            rawListing = listing,
                            menuItemIndex = -1
                        )
                    )
                }
            }
            result
        }

        var selectedCategoryFilter by remember { mutableStateOf("All") }
        val categoryOptions = listOf("All", "Marketplace", "Rentals", "Services", "Jobs", "Food")

        val countsMap = remember(allDisplayItems) {
            val map = mutableMapOf<String, Int>()
            map["All"] = allDisplayItems.size
            map["Marketplace"] = allDisplayItems.count { it.category.equals("Marketplace", ignoreCase = true) }
            map["Rentals"] = allDisplayItems.count { it.category.equals("Rentals", ignoreCase = true) }
            map["Services"] = allDisplayItems.count { it.category.equals("Services", ignoreCase = true) }
            map["Jobs"] = allDisplayItems.count { it.category.equals("Jobs", ignoreCase = true) }
            map["Food"] = allDisplayItems.count { it.category.equals("Food", ignoreCase = true) || it.category.equals("Restaurants", ignoreCase = true) }
            map
        }

        val filteredItems = remember(allDisplayItems, selectedCategoryFilter) {
            if (selectedCategoryFilter.equals("All", ignoreCase = true)) {
                allDisplayItems
            } else if (selectedCategoryFilter.equals("Food", ignoreCase = true)) {
                allDisplayItems.filter { it.category.equals("Food", ignoreCase = true) || it.category.equals("Restaurants", ignoreCase = true) }
            } else {
                allDisplayItems.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
            }
        }

        Dialog(
            onDismissRequest = { showMyListingsDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFFFDF2F8)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showMyListingsDialog = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "My Listings",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoryOptions) { catLabel ->
                                val count = countsMap[catLabel] ?: 0
                                val isSelected = catLabel.equals(selectedCategoryFilter, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) Color(0xFFEC4899) else Color.White)
                                        .border(1.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFFCE4EC), RoundedCornerShape(20.dp))
                                        .clickable { selectedCategoryFilter = catLabel }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "$catLabel ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF1E1E2D)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No listings found in $selectedCategoryFilter.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredItems) { item ->
                                    val cat = item.category
                                    val (badgeBg, badgeText, fallbackIcon) = when {
                                        cat.equals("Marketplace", ignoreCase = true) -> Triple(Color(0xFFFCE4EC), Color(0xFFEC4899), Icons.Default.ShoppingBag)
                                        cat.equals("Rentals", ignoreCase = true) -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Default.DirectionsCar)
                                        cat.equals("Services", ignoreCase = true) -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Build)
                                        cat.equals("Jobs", ignoreCase = true) -> Triple(Color(0xFFFFF8E1), Color(0xFFD97706), Icons.Default.Work)
                                        cat.equals("Rooms", ignoreCase = true) -> Triple(Color(0xFFF3E5F5), Color(0xFF9C27B0), Icons.Default.Bed)
                                        else -> Triple(Color(0xFFFCE4EC), Color(0xFFEC4899), Icons.Default.Restaurant)
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(90.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                            ) {
                                                if (item.imageUrl.isNotBlank()) {
                                                    ListingImage(
                                                        imageUrl = item.imageUrl,
                                                        contentDescription = item.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(badgeBg),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(fallbackIcon, contentDescription = null, tint = badgeText, modifier = Modifier.size(36.dp))
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = item.title,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFF1E1E2D),
                                                            maxLines = 2
                                                        )

                                                        if (item.price.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            val cleanPriceText = if (item.price.startsWith("₹")) item.price else "₹ ${item.price}"
                                                            Text(
                                                                text = cleanPriceText,
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = Color(0xFFEC4899)
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(6.dp))

                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(badgeBg)
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = item.category,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = badgeText
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            itemToEdit = item
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                                                    }

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Button(
                                                        onClick = {
                                                            if (item.menuItemIndex >= 0) {
                                                                val raw = item.rawListing
                                                                val updatedList = raw.menuItems.toMutableList()
                                                                if (item.menuItemIndex in updatedList.indices) {
                                                                    updatedList.removeAt(item.menuItemIndex)
                                                                    val updatedListing = raw.copy(menuItems = updatedList)
                                                                    listingViewModel.updateListing(context, updatedListing, newImageUri = null) { error ->
                                                                        if (error == null) {
                                                                            scope.launch { snackbarHostState.showSnackbar("Item deleted successfully!") }
                                                                        } else {
                                                                            scope.launch { snackbarHostState.showSnackbar("Failed to delete item: $error") }
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                listingToDelete = item.rawListing
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    }

    if (itemToEdit != null) {
        val currentDisplay = itemToEdit!!
        var editTitle by remember(currentDisplay) { mutableStateOf(currentDisplay.title) }
        var editPrice by remember(currentDisplay) { mutableStateOf(currentDisplay.price) }
        var editImageUri by remember(currentDisplay) { mutableStateOf<Uri?>(null) }
        var editImageUrl by remember(currentDisplay) { mutableStateOf(currentDisplay.imageUrl) }
        var isSavingEdit by remember { mutableStateOf(false) }

        val editImagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                editImageUri = uri
            }
        }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = {
                Text("Edit Item Details", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = LovelyTextDark)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Item Image",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LovelyTextDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFCE4EC))
                            .border(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { editImagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (editImageUri != null) {
                            AsyncImage(
                                model = editImageUri,
                                contentDescription = "New Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (editImageUrl.isNotBlank()) {
                            ListingImage(
                                imageUrl = editImageUrl,
                                contentDescription = "Item Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tap to Change Image", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Item Name") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Price / Rate (e.g. ₹180)") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter Item Name") }
                            return@Button
                        }

                        isSavingEdit = true
                        scope.launch {
                            val newEncodedImg = if (editImageUri != null) {
                                listingViewModel.compressImageSync(context, editImageUri!!)
                            } else {
                                editImageUrl
                            }

                            val cleanPriceStr = if (editPrice.startsWith("₹") || editPrice.isBlank()) editPrice else "₹$editPrice"

                            if (currentDisplay.menuItemIndex >= 0) {
                                val raw = currentDisplay.rawListing
                                val updatedList = raw.menuItems.toMutableList()
                                if (currentDisplay.menuItemIndex in updatedList.indices) {
                                    val formattedSubItem = "$editTitle | $cleanPriceStr | $newEncodedImg"
                                    updatedList[currentDisplay.menuItemIndex] = formattedSubItem
                                    val updatedListing = raw.copy(menuItems = updatedList)
                                    listingViewModel.updateListing(context, updatedListing, newImageUri = null) { error ->
                                        isSavingEdit = false
                                        itemToEdit = null
                                        if (error == null) {
                                            scope.launch { snackbarHostState.showSnackbar("Item updated successfully!") }
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar("Failed to update item: $error") }
                                        }
                                    }
                                }
                            } else {
                                val raw = currentDisplay.rawListing
                                val updatedListing = raw.copy(
                                    title = editTitle,
                                    price = cleanPriceStr,
                                    imageUrl = if (newEncodedImg.isNotBlank()) newEncodedImg else raw.imageUrl
                                )
                                listingViewModel.updateListing(context, updatedListing, newImageUri = editImageUri) { error ->
                                    isSavingEdit = false
                                    itemToEdit = null
                                    if (error == null) {
                                        scope.launch { snackbarHostState.showSnackbar("Item updated successfully!") }
                                    } else {
                                        scope.launch { snackbarHostState.showSnackbar("Failed to update item: $error") }
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isSavingEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = LovelyDarkPinkAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSavingEdit) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
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

    if (showDeleteAccountDialog) {
        var confirmText by remember { mutableStateOf("") }
        var isDeletingAccount by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) showDeleteAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Account Permanently",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1E2D)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFEBEE))
                            .border(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ Warning: This action cannot be undone. Your profile, shop listings, and account data will be permanently deleted.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC62828),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "To confirm deletion, please type \"DELETE\" below:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LovelyTextDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmText,
                        onValueChange = { confirmText = it },
                        placeholder = { Text("Type DELETE here") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (confirmText.trim() == "DELETE") {
                            isDeletingAccount = true
                            viewModel.deleteUserAccount(context) { _ ->
                                isDeletingAccount = false
                                showDeleteAccountDialog = false
                                onSignOut()
                            }
                        }
                    },
                    enabled = confirmText.trim() == "DELETE" && !isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Confirm Delete Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isDeletingAccount
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }

    if (showClockPickerFor != null) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                val hour12 = when (selectedHour) {
                    0 -> 12
                    in 1..12 -> selectedHour
                    else -> selectedHour - 12
                }
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, selectedMinute, amPm)
                if (showClockPickerFor == "open") {
                    openTime = formattedTime
                } else {
                    closeTime = formattedTime
                }
                showClockPickerFor = null
            },
            currentHour,
            currentMinute,
            false
        ).apply {
            setOnCancelListener { showClockPickerFor = null }
            show()
        }
    }
}
