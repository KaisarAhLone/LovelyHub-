package com.example.lovelyhub.ui.listings

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sell
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    val isStudent = userProfile?.role.isNullOrBlank() || userProfile?.role == "Student"

    val categories = remember(isStudent) {
        if (isStudent) {
            listOf("Jobs", "Marketplace", "Services")
        } else {
            listOf("Food", "Rooms", "Rentals", "Jobs", "Marketplace", "Services")
        }
    }

    var selectedCategory by remember(categories) { mutableStateOf(categories[0]) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var title by remember(userProfile) { mutableStateOf(userProfile?.businessName ?: "") }
    var price by remember { mutableStateOf("") }
    var phone by remember(userProfile) { mutableStateOf(userProfile?.businessPhone ?: "") }
    var location by remember(userProfile) { mutableStateOf(userProfile?.businessLocation ?: "") }
    var menuInput by remember { mutableStateOf("") }
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
            scope.launch { snackbarHostState.showSnackbar("Location permission required for GPS") }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            imageUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ADD NEW LISTING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E1E2D)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color(0xFFF6F7FB)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                            shape = RoundedCornerShape(12.dp),
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
                                    text = "Students can post in Jobs, Marketplace, & Services. To list Food, Rooms, or Rentals, please register as a Business Provider / Shopkeeper.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE65100),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF7926E1)) },
                                    trailingIcon = {
                                        IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF7926E1))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF7926E1),
                                        unfocusedBorderColor = Color(0xFFE0E0E0)
                                    )
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
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFF0E5FC))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
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
                                            tint = Color(0xFF7926E1),
                                            modifier = Modifier.size(38.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Upload Image / Photo",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF7926E1)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Shop / Item Name (e.g. Zaika Restro, DSA Book)") },
                                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color(0xFF7926E1)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                label = { Text("Price (e.g. ₹180 or ₹5000/month)") },
                                leadingIcon = { Icon(Icons.Default.Sell, contentDescription = null, tint = Color(0xFF7926E1)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = location,
                                    onValueChange = { location = it },
                                    label = { Text("Campus / Shop Location") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF7926E1)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isFetchingLocation) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF7926E1))
                                    } else {
                                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF7926E1), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Use Current GPS Location", fontSize = 13.sp, color = Color(0xFF7926E1), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Contact Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF7926E1)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = menuInput,
                                onValueChange = { menuInput = it },
                                label = { Text("Menu / Offerings Items (comma separated e.g. Biryani - ₹180, Coffee - ₹90)") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = Color(0xFF7926E1)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (title.isBlank() || price.isBlank() || phone.isBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar("Please enter Name, Price and Phone Number") }
                                        return@Button
                                    }

                                    isLoading = true
                                    val postedCat = selectedCategory
                                    val menuList = menuInput.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                    val newListing = Listing(
                                        category = postedCat,
                                        title = title,
                                        price = price,
                                        phone = phone,
                                        location = location,
                                        menuItems = menuList
                                    )

                                    viewModel.postListing(context, newListing, imageUri) { error ->
                                        isLoading = false
                                        if (error == null) {
                                            title = ""
                                            price = ""
                                            phone = ""
                                            location = ""
                                            menuInput = ""
                                            imageUri = null
                                            onSuccess(postedCat)
                                        } else {
                                            scope.launch { snackbarHostState.showSnackbar(error) }
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7926E1)),
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
                                    Text("Post Listing Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
