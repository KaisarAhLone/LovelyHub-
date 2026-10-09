package com.example.lovelyhub.ui.listings

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
        when (role) {
            "Restaurant Owner" -> listOf("Restaurants")
            "Room / PG Owner" -> listOf("Rooms")
            "Rental Provider" -> listOf("Rentals")
            "Service Provider" -> listOf("Services")
            else -> listOf("Jobs", "Marketplace", "Services")
        }
    }

    var selectedCategory by remember(categories, defaultCat) { mutableStateOf(defaultCat) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val categoryType = remember(selectedCategory) {
        when {
            selectedCategory.equals("Restaurants", ignoreCase = true) || selectedCategory.equals("Food", ignoreCase = true) -> "Food"
            selectedCategory.equals("Rooms", ignoreCase = true) -> "Rooms"
            selectedCategory.equals("Rentals", ignoreCase = true) -> "Rentals"
            selectedCategory.equals("Jobs", ignoreCase = true) -> "Jobs"
            else -> "Standard"
        }
    }

    var itemName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var roomType by remember { mutableStateOf("Single Room") }
    var isRoomTypeDropdownExpanded by remember { mutableStateOf(false) }

    var jobType by remember { mutableStateOf("Part-time") }
    var isJobTypeDropdownExpanded by remember { mutableStateOf(false) }
    var workHours by remember { mutableStateOf("4 PM - 9 PM") }
    var jobDesc by remember { mutableStateOf("") }

    var hasWifi by remember { mutableStateOf(true) }
    var hasBathroom by remember { mutableStateOf(true) }
    var hasWater by remember { mutableStateOf(true) }
    var hasFurnished by remember { mutableStateOf(true) }
    var extraNotes by remember { mutableStateOf("") }

    var phone by remember(userProfile) { mutableStateOf(userProfile?.businessPhone ?: "") }
    var location by remember(userProfile) { mutableStateOf(userProfile?.businessLocation ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var itemImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }

    val roomTypeOptions = listOf("Single Room", "Double Sharing", "Triple Sharing", "1BHK Flat")
    val jobTypeOptions = listOf("Part-time", "Full-time", "Internship", "Freelance / On-Demand")

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

    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            itemImageUris = uris.take(5)
            imageUri = uris.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (categoryType) {
                            "Food" -> "ADD DISH TO MENU"
                            "Rooms" -> "ADD ROOM TO PG"
                            "Rentals" -> "ADD VEHICLE TO RENTAL"
                            "Jobs" -> "POST CAMPUS JOB"
                            else -> "ADD NEW LISTING"
                        },
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
                                        if (categories.size > 1) {
                                            IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF673AB7))
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (categories.size > 1) {
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
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            when (categoryType) {
                                "Jobs" -> {
                                    OutlinedTextField(
                                        value = itemName,
                                        onValueChange = { itemName = it },
                                        label = { Text("Job Title / Designation (e.g. Cafe Barista)") },
                                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = jobType,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Job Type") },
                                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF673AB7)) },
                                            trailingIcon = {
                                                IconButton(onClick = { isJobTypeDropdownExpanded = true }) {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF673AB7))
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        DropdownMenu(
                                            expanded = isJobTypeDropdownExpanded,
                                            onDismissRequest = { isJobTypeDropdownExpanded = false },
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        ) {
                                            jobTypeOptions.forEach { opt ->
                                                DropdownMenuItem(
                                                    text = { Text(opt, fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        jobType = opt
                                                        isJobTypeDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = price,
                                        onValueChange = { price = it },
                                        label = { Text("Salary / Pay Rate (e.g. ₹12,000 / month)") },
                                        leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = workHours,
                                        onValueChange = { workHours = it },
                                        label = { Text("Work Hours / Timings (e.g. 4 PM - 9 PM)") },
                                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = jobDesc,
                                        onValueChange = { jobDesc = it },
                                        label = { Text("Job Description / Requirements") },
                                        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = location,
                                            onValueChange = { location = it },
                                            label = { Text("Job Location (e.g. Law Gate, LPU)") },
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
                                                onClick = { MapLocationHelper.openMapPicker(context, location) },
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
                                        label = { Text("Contact Phone Number") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF673AB7)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = when (categoryType) {
                                            "Food" -> "Dish Photo"
                                            "Rooms" -> "Room Photos (Upload up to 5 Photos)"
                                            "Rentals" -> "Vehicle Photos (Upload up to 5 Photos)"
                                            else -> "Photo"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (categoryType == "Rooms" || categoryType == "Rentals") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFF0E5FC))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                                .clickable { multipleImagePickerLauncher.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (itemImageUris.isNotEmpty()) {
                                                LazyRow(
                                                    modifier = Modifier.fillMaxSize().padding(8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    items(itemImageUris) { uri ->
                                                        AsyncImage(
                                                            model = uri,
                                                            contentDescription = "Photo",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier
                                                                .size(140.dp)
                                                                .clip(RoundedCornerShape(12.dp))
                                                        )
                                                    }
                                                }
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
                                                        text = if (categoryType == "Rooms") "Upload Room Photos (Up to 5)" else "Upload Vehicle Photos (Up to 5)",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF673AB7)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFF0E5FC))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                                .clickable { singleImagePickerLauncher.launch("image/*") },
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
                                                        text = if (categoryType == "Food") "Upload Dish Photo" else "Upload Photo",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF673AB7)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    when (categoryType) {
                                        "Rooms" -> {
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedTextField(
                                                    value = roomType,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text("Room Type") },
                                                    leadingIcon = { Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color(0xFF673AB7)) },
                                                    trailingIcon = {
                                                        IconButton(onClick = { isRoomTypeDropdownExpanded = true }) {
                                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF673AB7))
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp)
                                                )

                                                DropdownMenu(
                                                    expanded = isRoomTypeDropdownExpanded,
                                                    onDismissRequest = { isRoomTypeDropdownExpanded = false },
                                                    modifier = Modifier.fillMaxWidth(0.85f)
                                                ) {
                                                    roomTypeOptions.forEach { option ->
                                                        DropdownMenuItem(
                                                            text = { Text(option, fontWeight = FontWeight.Medium) },
                                                            onClick = {
                                                                roomType = option
                                                                isRoomTypeDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            OutlinedTextField(
                                                value = price,
                                                onValueChange = { price = it },
                                                label = { Text("Rent Price (e.g. ₹5000/month)") },
                                                leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF673AB7)) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            Text(
                                                text = "Room Facilities & Amenities",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1E1E2D)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Checkbox(
                                                    checked = hasWifi,
                                                    onCheckedChange = { hasWifi = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                                                )
                                                Text("Wifi Available", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Checkbox(
                                                    checked = hasBathroom,
                                                    onCheckedChange = { hasBathroom = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                                                )
                                                Text("Attached Bathroom", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Checkbox(
                                                    checked = hasWater,
                                                    onCheckedChange = { hasWater = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                                                )
                                                Text("24x7 Water", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Checkbox(
                                                    checked = hasFurnished,
                                                    onCheckedChange = { hasFurnished = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF673AB7))
                                                )
                                                Text("Fully Furnished", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            OutlinedTextField(
                                                value = extraNotes,
                                                onValueChange = { extraNotes = it },
                                                label = { Text("Extra Facilities (e.g. AC, Balcony, Geyser)") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                        "Rentals" -> {
                                            OutlinedTextField(
                                                value = itemName,
                                                onValueChange = { itemName = it },
                                                label = { Text("Vehicle Name (e.g. Royal Enfield Bullet 350)") },
                                                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF673AB7)) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            OutlinedTextField(
                                                value = price,
                                                onValueChange = { price = it },
                                                label = { Text("Rental Price (e.g. ₹500/day or ₹60/hour)") },
                                                leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF673AB7)) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

                                            OutlinedTextField(
                                                value = mileage,
                                                onValueChange = { mileage = it },
                                                label = { Text("Mileage / Features (e.g. 45 km/l, Petrol)") },
                                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF673AB7)) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                        }
                                        else -> {
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
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (categoryType == "Food") ImeAction.Done else ImeAction.Next),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )

                                            if (categoryType == "Standard") {
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
                                                            onClick = { MapLocationHelper.openMapPicker(context, location) },
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
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(26.dp))

                            Button(
                                onClick = {
                                    val finalItemTitle = when (categoryType) {
                                        "Rooms" -> roomType
                                        "Rentals" -> if (mileage.isNotBlank()) "$itemName ($mileage)" else itemName
                                        "Jobs" -> "$itemName ($jobType)"
                                        else -> itemName
                                    }

                                    if (finalItemTitle.isBlank() || price.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please enter required details and price/salary") }
                                        return@Button
                                    }

                                    isLoading = true
                                    val postedCat = selectedCategory

                                    val finalPhone = if (categoryType != "Standard" && categoryType != "Jobs") (userProfile?.businessPhone ?: "") else phone
                                    val finalLocation = if (categoryType != "Standard" && categoryType != "Jobs") (userProfile?.businessLocation ?: "") else location

                                    scope.launch {
                                        val encodedImagesList = mutableListOf<String>()
                                        if ((categoryType == "Rooms" || categoryType == "Rentals") && itemImageUris.isNotEmpty()) {
                                            itemImageUris.forEach { u ->
                                                val enc = viewModel.compressImageSync(context, u)
                                                if (enc.isNotBlank()) encodedImagesList.add(enc)
                                            }
                                        }

                                        val finalCombinedImages = if (encodedImagesList.isNotEmpty()) {
                                            encodedImagesList.joinToString("~")
                                        } else {
                                            ""
                                        }

                                        val selectedFacs = mutableListOf<String>()
                                        if (categoryType == "Rooms") {
                                            if (hasWifi) selectedFacs.add("Wifi")
                                            if (hasBathroom) selectedFacs.add("Attached Bathroom")
                                            if (hasWater) selectedFacs.add("24x7 Water")
                                            if (hasFurnished) selectedFacs.add("Furnished")
                                            if (extraNotes.isNotBlank()) selectedFacs.add(extraNotes.trim())
                                        }

                                        val finalFacilitiesString = selectedFacs.joinToString(",")

                                        val finalFormattedItem = when (categoryType) {
                                            "Rooms" -> if (finalFacilitiesString.isNotBlank()) "$finalItemTitle - $price|$finalCombinedImages|$finalFacilitiesString" else ""
                                            "Jobs" -> "$jobDesc|$workHours"
                                            else -> ""
                                        }

                                        val newListing = Listing(
                                            category = postedCat,
                                            title = finalItemTitle,
                                            price = price,
                                            phone = finalPhone,
                                            location = finalLocation,
                                            imageUrl = finalCombinedImages,
                                            menuItems = if (finalFormattedItem.isNotBlank()) listOf(finalFormattedItem) else emptyList()
                                        )

                                        viewModel.postListing(context, newListing, if (categoryType == "Rooms" || categoryType == "Rentals") null else imageUri) { error ->
                                            isLoading = false
                                            if (error == null) {
                                                itemName = ""
                                                price = ""
                                                mileage = ""
                                                jobDesc = ""
                                                imageUri = null
                                                itemImageUris = emptyList()
                                                scope.launch { snackbarHostState.showSnackbar("Added successfully!") }
                                                onSuccess(postedCat)
                                            } else {
                                                scope.launch { snackbarHostState.showSnackbar(error) }
                                            }
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
