package com.example.lovelyhub.ui.listings

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.components.MapLocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListingScreen(
    viewModel: ListingViewModel,
    authViewModel: AuthViewModel,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val userProfile by authViewModel.currentUserProfile.collectAsState()
    val role = userProfile?.role ?: "Student"
    val isStudent = role == "Student"

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFFFFB300)
        )
    )

    val defaultCat = when (role) {
        "Restaurant Owner" -> "Restaurants"
        "Room / PG Owner" -> "Rooms"
        "Rental Provider" -> "Rentals"
        "Service Provider" -> "Services"
        else -> "Marketplace"
    }

    val categories = remember(role) {
        if (isStudent) {
            listOf("Jobs", "Marketplace", "Services")
        } else {
            listOf("Restaurants", "Rooms", "Rentals", "Jobs", "Marketplace", "Services")
        }
    }

    var selectedCategory by remember(categories, defaultCat) { mutableStateOf(defaultCat) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val isBusinessCategory = selectedCategory.equals("Restaurants", ignoreCase = true) ||
            selectedCategory.equals("Food", ignoreCase = true) ||
            selectedCategory.equals("Rooms", ignoreCase = true) ||
            selectedCategory.equals("Rentals", ignoreCase = true)

    var itemName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var phone by remember(userProfile) { mutableStateOf(userProfile?.businessPhone ?: "") }
    var location by remember(userProfile) { mutableStateOf(userProfile?.businessLocation ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            isFetchingLocation = true
            MapLocationHelper.getCurrentGpsAddress(context) { gpsLoc ->
                isFetchingLocation = false
                location = gpsLoc
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
            imageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ADD ITEM",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF673AB7))
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        .padding(20.dp)
                ) {
                    if (isStudent) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Students can post in Jobs, Marketplace, & Services. To list Food, Rooms, or Rentals, please register as an Owner/Provider.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE65100),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else if (userProfile?.businessName?.isNotBlank() == true) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = Color(0xFF673AB7),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = userProfile?.businessName ?: "My Shop",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF673AB7)
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Category",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E1E2D)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF673AB7)) },
                                    trailingIcon = {
                                        IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF673AB7))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                DropdownMenu(
                                    expanded = isCategoryDropdownExpanded,
                                    onDismissRequest = { isCategoryDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                selectedCategory = cat
                                                isCategoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Photo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E1E2D)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF0E5FC))
                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri != null) {
                                    AsyncImage(
                                        model = imageUri,
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
                                            modifier = Modifier.size(42.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Upload Photo",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF673AB7)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = itemName,
                                onValueChange = { itemName = it },
                                label = { Text("Item Name") },
                                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color(0xFF673AB7)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Price") },
                                leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF673AB7)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (isBusinessCategory) ImeAction.Done else ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            if (!isBusinessCategory) {
                                Spacer(modifier = Modifier.height(14.dp))

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = location,
                                        onValueChange = { location = it },
                                        label = { Text("Location") },
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
                                                MapLocationHelper.openMapPicker(context, location)
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
                                            if (isFetchingLocation) {
                                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF673AB7))
                                            } else {
                                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF673AB7), modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Current GPS", fontSize = 12.sp, color = Color(0xFF673AB7), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF673AB7)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = {
                                    if (itemName.isBlank() || price.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please enter Item Name and Price") }
                                        return@Button
                                    }

                                    isLoading = true
                                    val postedCat = selectedCategory

                                    val finalPhone = if (isBusinessCategory) (userProfile?.businessPhone ?: "") else phone
                                    val finalLocation = if (isBusinessCategory) (userProfile?.businessLocation ?: "") else location
                                    val finalOpenTime = userProfile?.openTime.takeIf { !it.isNullOrBlank() } ?: "09:00 AM"
                                    val finalCloseTime = userProfile?.closeTime.takeIf { !it.isNullOrBlank() } ?: "10:00 PM"
                                    val final24Hours = userProfile?.is24Hours ?: false

                                    val newListing = Listing(
                                        category = postedCat,
                                        title = itemName,
                                        price = price,
                                        phone = finalPhone,
                                        location = finalLocation,
                                        openTime = finalOpenTime,
                                        closeTime = finalCloseTime,
                                        is24Hours = final24Hours
                                    )

                                    viewModel.postListing(context, newListing, imageUri) { error ->
                                        isLoading = false
                                        if (error == null) {
                                            itemName = ""
                                            price = ""
                                            imageUri = null
                                            scope.launch { snackbarHostState.showSnackbar("Item added successfully!") }
                                            onSuccess(postedCat)
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar(error) }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Add", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
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
    }
}
