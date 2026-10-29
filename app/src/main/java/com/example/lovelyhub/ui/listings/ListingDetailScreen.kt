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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Shower
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.components.MapLocationHelper
import kotlinx.coroutines.launch

data class LocalCartItem(
    val name: String,
    val priceText: String,
    val priceNum: Double,
    val photoUrl: String,
    var quantity: Int = 1
)

data class RoomFeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color
)

data class SubRoomDetail(
    val name: String,
    val priceText: String,
    val photoUrl: String
)

data class RentalCarItem(
    val modelName: String,
    val bodyType: String,
    val seats: String,
    val fuelType: String,
    val transmission: String,
    val bags: String,
    val rate1hr: String,
    val rate3hr: String,
    val rate6hr: String,
    val rateDay: String,
    val photoUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: Listing,
    onBackClick: () -> Unit,
    onMessageClick: (receiverUid: String, receiverName: String, receiverPhotoUrl: String) -> Unit = { _, _, _ -> },
    onSendOrderClick: (receiverUid: String, receiverName: String, receiverPhotoUrl: String, orderText: String) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isRoomCategory = listing.category.equals("Rooms", ignoreCase = true)
    val isRentalCategory = listing.category.equals("Rentals", ignoreCase = true)
    val isJobCategory = listing.category.equals("Jobs", ignoreCase = true)
    val isMarketplaceCategory = listing.category.equals("Marketplace", ignoreCase = true)
    val isNoCartCategory = isRoomCategory || isRentalCategory || isJobCategory || isMarketplaceCategory

    val cart = remember { mutableStateListOf<LocalCartItem>() }
    var showCartDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedSubRoom by remember { mutableStateOf<SubRoomDetail?>(null) }

    var foodSearchQuery by remember { mutableStateOf("") }
    var selectedFoodCategory by remember { mutableStateOf("All") }

    val rawPhotos = remember(listing.imageUrl) {
        listing.imageUrl.split("~").filter { it.isNotBlank() }
    }
    val galleryPhotos = if (rawPhotos.isNotEmpty()) rawPhotos else listOf(listing.imageUrl)
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var custPgName by remember { mutableStateOf("") }
    var custLocation by remember { mutableStateOf("") }
    var isFetchingGps by remember { mutableStateOf(false) }

    val roomFeatures = remember {
        listOf(
            RoomFeatureItem("WiFi", "Available", Icons.Default.Wifi, Color(0xFFFCE4EC), Color(0xFFEC407A)),
            RoomFeatureItem("Attached", "Bathroom", Icons.Default.Shower, Color(0xFFFFEBEE), Color(0xFFEF5350)),
            RoomFeatureItem("24x7", "Water", Icons.Default.WaterDrop, Color(0xFFE3F2FD), Color(0xFF42A5F5)),
            RoomFeatureItem("Electricity", "Included", Icons.Default.Bolt, Color(0xFFFFF8E1), Color(0xFFFFA726)),
            RoomFeatureItem("Fully", "Furnished", Icons.Default.Bed, Color(0xFFF3E5F5), Color(0xFFAB47BC)),
            RoomFeatureItem("AC", "Available", Icons.Default.AcUnit, Color(0xFFE0F7FA), Color(0xFF26C6DA))
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            isFetchingGps = true
            MapLocationHelper.getCurrentGpsAddress(context) { gpsLoc ->
                isFetchingGps = false
                custLocation = gpsLoc
                scope.launch { snackbarHostState.showSnackbar("GPS Location detected!") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Location permission required") }
        }
    }

    fun cleanLocationName(rawLocation: String): String {
        if (rawLocation.isBlank()) return "LPU Campus"
        val idx = rawLocation.indexOf("(")
        return if (idx != -1) rawLocation.substring(0, idx).trim() else rawLocation.trim()
    }

    val displayLocation = cleanLocationName(listing.location)

    fun parsePriceNumber(raw: String): Double {
        val numStr = raw.replace(Regex("[^0-9.]"), "")
        return numStr.toDoubleOrNull() ?: 100.0
    }

    val totalItemsCount = cart.sumOf { it.quantity }
    val totalCartPrice = cart.sumOf { it.priceNum * it.quantity }

    val pinkHeaderGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (!isNoCartCategory && cart.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2D)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$totalItemsCount ITEMS | ₹${totalCartPrice.toInt()}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Delivery ₹0 • Platform Fee ₹0",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showCartDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Cart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDF2F8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(pinkHeaderGradient)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (selectedSubRoom != null) {
                                            selectedSubRoom = null
                                        } else {
                                            onBackClick()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isMarketplaceCategory) "Item Details" else (if (isRentalCategory) listing.title.ifBlank { "Car Rentals" } else (if (isJobCategory) "Job Details" else (if (isRoomCategory) (if (selectedSubRoom != null) "Room Details" else "PG Details") else listing.title))),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    if (!isMarketplaceCategory) {
                                        Text(
                                            text = if (isRentalCategory) "View available vehicles and rental options" else "Find trusted services near your campus",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .clickable { },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (listing.isFavorite) Color(0xFFFF1744) else Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .clickable {
                                            val shareMsg = "Check out '${listing.title}' on LovelyHub!\nLocation: ${listing.location}"
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, shareMsg)
                                            }
                                            try { context.startActivity(Intent.createChooser(intent, "Share Details")) } catch (_: Exception) {}
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    if (isMarketplaceCategory) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(18.dp))
                            ) {
                                val activePhoto = galleryPhotos.getOrElse(selectedPhotoIndex) { galleryPhotos.firstOrNull() ?: "" }
                                if (activePhoto.isNotBlank()) {
                                    ListingImage(
                                        imageUrl = activePhoto,
                                        contentDescription = listing.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { expandedImageUrl = activePhoto }
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFFCE4EC)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = null,
                                            tint = Color(0xFFEC4899),
                                            modifier = Modifier.size(64.dp)
                                        )
                                    }
                                }

                                if (galleryPhotos.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "📷 ${selectedPhotoIndex + 1}/${galleryPhotos.size}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (galleryPhotos.size > 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    itemsIndexed(galleryPhotos) { idx, photo ->
                                        val isSel = idx == selectedPhotoIndex
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    width = if (isSel) 2.5.dp else 1.dp,
                                                    color = if (isSel) Color(0xFFEC4899) else Color.LightGray,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { selectedPhotoIndex = idx }
                                        ) {
                                            ListingImage(
                                                imageUrl = photo,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = listing.title,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    val formattedPrice = if (listing.price.startsWith("₹")) listing.price else "₹ ${listing.price}"
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formattedPrice,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFEC4899)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("(Negotiable)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(displayLocation, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", fontWeight = FontWeight.Bold, color = Color(0xFFEC4899), fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFCE4EC)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(24.dp))
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text("Campus Seller", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E2D))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("4.8 Rating", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Description", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E2D))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = listing.menuItems.firstOrNull() ?: "Selling my item in excellent condition. Works perfectly for college use, coding, and daily tasks. Feel free to contact for more details or to check the item in person.",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 19.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (listing.phone.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Button(
                                    onClick = {
                                        val num = listing.whatsApp.ifBlank { listing.phone }
                                        if (num.isNotBlank()) {
                                            val cleanNum = num.replace(Regex("[^0-9]"), "")
                                            val fullNum = if (!cleanNum.startsWith("91") && cleanNum.length == 10) "91$cleanNum" else cleanNum
                                            val waUri = Uri.parse("https:" + "/" + "/api.whatsapp.com/send?phone=$fullNum&text=" + Uri.encode("Hello! I am interested in buying your ${listing.title} on LovelyHub."))
                                            try { context.startActivity(Intent(Intent.ACTION_VIEW, waUri)) } catch (_: Exception) {}
                                        }
                                    },
                                    modifier = Modifier.weight(1.2f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                ) {
                                    Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat on WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    } else if (isRentalCategory) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                    ) {
                                        val activePhoto = galleryPhotos.getOrElse(selectedPhotoIndex) { galleryPhotos.firstOrNull() ?: "" }
                                        if (activePhoto.isNotBlank()) {
                                            ListingImage(
                                                imageUrl = activePhoto,
                                                contentDescription = listing.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { expandedImageUrl = activePhoto }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFE8EAF6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEC4899),
                                                    modifier = Modifier.size(64.dp)
                                                )
                                            }
                                        }

                                        if (galleryPhotos.size > 1) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(10.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "📷 1/${galleryPhotos.size}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1E1E2D)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsCar,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB703),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = listing.title,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1E1E2D)
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(listing.rating.ifBlank { "4.3" }, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E1E2D))
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(displayLocation, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            }
                                        }

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (listing.phone.isNotBlank()) {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val num = listing.whatsApp.ifBlank { listing.phone }
                                                    if (num.isNotBlank()) {
                                                        val cleanNum = num.replace(Regex("[^0-9]"), "")
                                                        val fullNum = if (!cleanNum.startsWith("91") && cleanNum.length == 10) "91$cleanNum" else cleanNum
                                                        val waUri = Uri.parse("https:" + "/" + "/api.whatsapp.com/send?phone=$fullNum&text=" + Uri.encode("Hello! I am interested in renting a vehicle from ${listing.title} on LovelyHub."))
                                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, waUri)) } catch (_: Exception) {}
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Column {
                                Text(
                                    text = "Available Vehicles",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1E2D)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .width(45.dp)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFEC4899))
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val rentalCars = if (listing.menuItems.isNotEmpty()) {
                                listing.menuItems.map { rawItem ->
                                    val parts = rawItem.split("|").map { it.trim() }
                                    val name = parts.getOrNull(0) ?: "Vehicle"
                                    val type = parts.getOrNull(1) ?: "Car"
                                    val seats = parts.getOrNull(2) ?: "5 Seats"
                                    val fuel = parts.getOrNull(3) ?: "Petrol"
                                    val trans = parts.getOrNull(4) ?: "Manual"
                                    val bags = parts.getOrNull(5) ?: "2 Bags"
                                    val r1 = parts.getOrNull(6) ?: "₹150"
                                    val r3 = parts.getOrNull(7) ?: "₹800"
                                    val r6 = parts.getOrNull(8) ?: "₹1,200"
                                    val rDay = parts.getOrNull(9) ?: "₹2,000"
                                    val photo = parts.getOrNull(10) ?: galleryPhotos.firstOrNull() ?: ""
                                    RentalCarItem(name, type, seats, fuel, trans, bags, r1, r3, r6, rDay, photo)
                                }
                            } else {
                                emptyList()
                            }

                            if (rentalCars.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No vehicles added yet by owner.",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    rentalCars.forEach { car ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 110.dp, height = 110.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                    ) {
                                                        if (car.photoUrl.isNotBlank()) {
                                                            ListingImage(
                                                                imageUrl = car.photoUrl,
                                                                contentDescription = car.modelName,
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .clickable { expandedImageUrl = car.photoUrl }
                                                            )
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .background(Color(0xFFFCE4EC)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(42.dp))
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.width(12.dp))

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = car.modelName,
                                                                    fontSize = 16.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = Color(0xFF1E1E2D)
                                                                )

                                                                Spacer(modifier = Modifier.width(6.dp))

                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(Color(0xFFFCE4EC))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text(car.bodyType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                                                                }
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFFCE4EC)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(15.dp))
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(6.dp))

                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("👤 ${car.seats}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                            Text("⛽ ${car.fuelType}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                            Text("⚙️ ${car.transmission}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                            Text("🧳 ${car.bags}", fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .border(1.dp, Color(0xFFEC4899), RoundedCornerShape(8.dp))
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(if (car.rate1hr.startsWith("₹")) car.rate1hr else "₹${car.rate1hr}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEC4899))
                                                                    Text("Per Hour", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                                }
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFFFCE4EC))
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(if (car.rate3hr.startsWith("₹")) car.rate3hr else "₹${car.rate3hr}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEC4899))
                                                                    Text("3 Hours", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                                }
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFFFCE4EC))
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(if (car.rate6hr.startsWith("₹")) car.rate6hr else "₹${car.rate6hr}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEC4899))
                                                                    Text("6 Hours", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                                }
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(Color(0xFFFCE4EC))
                                                                    .padding(vertical = 4.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                    Text(if (car.rateDay.startsWith("₹")) car.rateDay else "₹${car.rateDay}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEC4899))
                                                                    Text("Per Day", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            if (listing.ownerUid.isNotBlank()) {
                                                                onMessageClick(listing.ownerUid, "${car.modelName} Rental Booking", listing.imageUrl)
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f).height(40.dp),
                                                        shape = RoundedCornerShape(10.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(15.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Rent Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            if (listing.phone.isNotBlank()) {
                                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                                try { context.startActivity(intent) } catch (_: Exception) {}
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f).height(40.dp),
                                                        shape = RoundedCornerShape(10.dp),
                                                        border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("View Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFEC4899))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isJobCategory) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF3D42DF), Color(0xFF4338CA))
                                                )
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Work, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text(
                                                        text = listing.title,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 17.sp
                                                    )
                                                    Text(
                                                        text = "Campus Job Offer",
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.25f))
                                                    .clickable { },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = null,
                                                    tint = if (listing.isFavorite) Color(0xFFFF1744) else Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = listing.title,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1E2D)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = displayLocation,
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("Job Type", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                        Text(if (listing.price.contains("Full", ignoreCase = true)) "Full-time" else "Part-time", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                                                    }
                                                }
                                            }

                                            Card(
                                                modifier = Modifier.weight(1.2f),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text("Work Schedule", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                        Text("Flexible hours", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.White),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Paid, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                                }

                                                Spacer(modifier = Modifier.width(12.dp))

                                                Column {
                                                    Text("Pay", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                                    Text(
                                                        text = if (listing.price.isNotBlank() && !listing.price.contains("Open")) listing.price else "As listed by employer",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFD97706)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))
                                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                                        Spacer(modifier = Modifier.height(18.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFCE4EC)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Job Description", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E2D))
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = "Assist with daily store tasks and help students.",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 19.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 46.dp)
                                        )

                                        Spacer(modifier = Modifier.height(18.dp))
                                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                                        Spacer(modifier = Modifier.height(18.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFCE4EC)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Requirements", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E2D))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        val reqList = listing.menuItems

                                        if (reqList.isEmpty()) {
                                            Text(
                                                text = "No specific requirements listed.",
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 46.dp)
                                            )
                                        } else {
                                            Column(
                                                modifier = Modifier.padding(start = 46.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                reqList.forEach { req ->
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFEC4899))
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(req, fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(22.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (listing.phone.isNotBlank()) {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Call Employer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    val num = listing.whatsApp.ifBlank { listing.phone }
                                                    if (num.isNotBlank()) {
                                                        val cleanNum = num.replace(Regex("[^0-9]"), "")
                                                        val fullNum = if (!cleanNum.startsWith("91") && cleanNum.length == 10) "91$cleanNum" else cleanNum
                                                        val waUri = Uri.parse("https:" + "/" + "/api.whatsapp.com/send?phone=$fullNum&text=" + Uri.encode("Hello! I am interested in applying for ${listing.title} on LovelyHub."))
                                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, waUri)) } catch (_: Exception) {}
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                                            ) {
                                                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Button(
                                            onClick = { MapLocationHelper.openMapDirections(context, listing.location) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isRoomCategory && selectedSubRoom == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                    ) {
                                        val activePhoto = galleryPhotos.getOrElse(selectedPhotoIndex) { galleryPhotos.firstOrNull() ?: "" }
                                        if (activePhoto.isNotBlank()) {
                                            ListingImage(
                                                imageUrl = activePhoto,
                                                contentDescription = listing.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { expandedImageUrl = activePhoto }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFE8EAF6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.HomeWork,
                                                    contentDescription = null,
                                                    tint = Color(0xFF3F51B5),
                                                    modifier = Modifier.size(64.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 10.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                galleryPhotos.take(5).forEachIndexed { idx, _ ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(if (idx == selectedPhotoIndex) 10.dp else 7.dp)
                                                            .clip(CircleShape)
                                                            .background(if (idx == selectedPhotoIndex) Color(0xFFEC4899) else Color.White.copy(alpha = 0.7f))
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = listing.title.ifBlank { "agi PG" },
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1E2D)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF2E7D32))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = listing.rating.ifBlank { "4.3" },
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = displayLocation,
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (listing.phone.isNotBlank()) {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        }

                                        Button(
                                            onClick = { MapLocationHelper.openMapDirections(context, listing.location) },
                                            modifier = Modifier.weight(1.4f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        }

                                        Button(
                                            onClick = {
                                                if (listing.ownerUid.isNotBlank()) {
                                                    onMessageClick(listing.ownerUid, listing.title, listing.imageUrl)
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFFEC4899)),
                                            border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Column {
                                Text(
                                    text = "Available Rooms",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1E2D)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .width(45.dp)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color(0xFFEC4899))
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val subRooms = if (listing.menuItems.isNotEmpty()) {
                                listing.menuItems.map { menuItemStr ->
                                    val parts = menuItemStr.split("|")
                                    val rName = parts.getOrNull(0)?.trim() ?: "Room"
                                    val rPrice = parts.getOrNull(1)?.trim() ?: listing.price
                                    val rPhoto = parts.getOrNull(2)?.trim() ?: galleryPhotos.firstOrNull() ?: ""
                                    SubRoomDetail(rName, rPrice, rPhoto)
                                }
                            } else {
                                emptyList()
                            }

                            if (subRooms.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No rooms added yet by owner.",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    subRooms.forEach { sub ->
                                        val cleanPrice = if (sub.priceText.startsWith("₹")) sub.priceText else "₹${sub.priceText}"
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedSubRoom = sub },
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (sub.photoUrl.isNotBlank()) {
                                                    ListingImage(
                                                        imageUrl = sub.photoUrl,
                                                        contentDescription = sub.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(Color(0xFFFCE4EC)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Bed, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(36.dp))
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(14.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = sub.name,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1E1E2D)
                                                    )

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = if (cleanPrice.contains("/")) cleanPrice else "$cleanPrice / month",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFEC4899)
                                                    )

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFFFCE4EC))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("🛏️ 1 Person", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFFE0F2FE))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("📶 WiFi", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(Color(0xFFFFEBEE))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("🚿 Attached", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                                        }
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFCE4EC)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isRoomCategory && selectedSubRoom != null) {
                        val activeSub = selectedSubRoom!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                    ) {
                                        val activePhoto = if (activeSub.photoUrl.isNotBlank()) activeSub.photoUrl else galleryPhotos.getOrElse(selectedPhotoIndex) { galleryPhotos.firstOrNull() ?: "" }
                                        if (activePhoto.isNotBlank()) {
                                            ListingImage(
                                                imageUrl = activePhoto,
                                                contentDescription = activeSub.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { expandedImageUrl = activePhoto }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFE8EAF6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bed,
                                                    contentDescription = null,
                                                    tint = Color(0xFF3F51B5),
                                                    modifier = Modifier.size(64.dp)
                                                )
                                            }
                                        }

                                        if (galleryPhotos.size > 1) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(10.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "📷 ${selectedPhotoIndex + 1} / ${galleryPhotos.size}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if (galleryPhotos.size > 1) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            itemsIndexed(galleryPhotos) { idx, photo ->
                                                val isSel = idx == selectedPhotoIndex
                                                Box(
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(
                                                            width = if (isSel) 2.5.dp else 1.dp,
                                                            color = if (isSel) Color(0xFFEC4899) else Color.LightGray,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { selectedPhotoIndex = idx }
                                                ) {
                                                    ListingImage(
                                                        imageUrl = photo,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeSub.name,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = displayLocation,
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = listing.rating.ifBlank { "4.3" },
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val cleanRent = if (activeSub.priceText.startsWith("₹")) activeSub.priceText else "₹${activeSub.priceText}"
                            Text(
                                text = if (cleanRent.contains("/")) cleanRent else "$cleanRent / month",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEC4899)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Room Features",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1E2D)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val featurePairs = roomFeatures.chunked(2)
                                featurePairs.forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pair.forEach { feat ->
                                            Card(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .clip(CircleShape)
                                                            .background(feat.iconBg),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(feat.icon, contentDescription = null, tint = feat.iconTint, modifier = Modifier.size(18.dp))
                                                    }

                                                    Spacer(modifier = Modifier.width(10.dp))

                                                    Column {
                                                        Text(
                                                            text = feat.title,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFF1E1E2D),
                                                            maxLines = 1
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = feat.subtitle,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color.Gray,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = "Description",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1E2D)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Spacious and fully furnished room suitable for students and working professionals. Safe and peaceful location with all basic facilities.",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (listing.phone.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        MapLocationHelper.openMapDirections(context, listing.location)
                                    },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        if (listing.ownerUid.isNotBlank()) {
                                            onMessageClick(listing.ownerUid, listing.title, listing.imageUrl)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEC4899)),
                                    border = BorderStroke(1.5.dp, Color(0xFFEC4899)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                    ) {
                                        val activePhoto = galleryPhotos.getOrElse(selectedPhotoIndex) { galleryPhotos.firstOrNull() ?: "" }
                                        if (activePhoto.isNotBlank()) {
                                            ListingImage(
                                                imageUrl = activePhoto,
                                                contentDescription = listing.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { expandedImageUrl = activePhoto }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFFE8EAF6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Restaurant,
                                                    contentDescription = null,
                                                    tint = Color(0xFFEC4899),
                                                    modifier = Modifier.size(64.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(10.dp)
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (listing.isFavorite) Color(0xFFFF1744) else Color(0xFFEC4899),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = listing.title,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1E2D)
                                        )

                                        Column(horizontalAlignment = Alignment.End) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF2E7D32))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = listing.rating.ifBlank { "4.3" },
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = displayLocation,
                                            fontSize = 13.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (listing.phone.isNotBlank()) {
                                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        }

                                        Button(
                                            onClick = { MapLocationHelper.openMapDirections(context, listing.location) },
                                            modifier = Modifier.weight(1.4f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        }

                                        Button(
                                            onClick = {
                                                if (listing.ownerUid.isNotBlank()) {
                                                    onMessageClick(listing.ownerUid, listing.title, listing.imageUrl)
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFFEC4899)),
                                            border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Chat", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Food Menu & Dishes",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(45.dp)
                                            .height(3.5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFFEC4899))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFFCE4EC), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box {
                                            if (foodSearchQuery.isEmpty()) {
                                                Text("Search dishes...", fontSize = 12.sp, color = Color.Gray)
                                            }
                                            BasicTextField(
                                                value = foodSearchQuery,
                                                onValueChange = { foodSearchQuery = it },
                                                singleLine = true,
                                                textStyle = TextStyle(fontSize = 12.sp, color = Color(0xFF1E1E2D), fontWeight = FontWeight.Medium),
                                                modifier = Modifier.width(90.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val foodFilterList = if (listing.menuItems.isNotEmpty()) {
                                val extracted = listing.menuItems.mapNotNull { itemStr ->
                                    val raw = itemStr.split("|").firstOrNull()?.trim() ?: ""
                                    val clean = if (raw.contains("-")) raw.split("-").first().trim() else raw.trim()
                                    clean.ifBlank { null }
                                }.distinct()
                                listOf("All") + extracted.take(8)
                            } else {
                                listOf("All")
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(foodFilterList) { catLabel ->
                                    val isSelected = catLabel.equals(selectedFoodCategory, ignoreCase = true)
                                    val emoji = when (catLabel) {
                                        "Biryani" -> "🍚 "
                                        "Chicken" -> "🍗 "
                                        "Tandoori" -> "🔥 "
                                        "Curries" -> "🍲 "
                                        "Snacks" -> "🍟 "
                                        else -> ""
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFFEC4899) else Color.White)
                                            .border(1.dp, if (isSelected) Color(0xFFEC4899) else Color(0xFFFCE4EC), RoundedCornerShape(16.dp))
                                            .clickable { selectedFoodCategory = catLabel }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$emoji$catLabel",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF1E1E2D)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val filteredDishes = if (listing.menuItems.isNotEmpty()) {
                                listing.menuItems.filter { menuItemStr ->
                                    val matchesQuery = foodSearchQuery.isBlank() || menuItemStr.contains(foodSearchQuery.trim(), ignoreCase = true)
                                    val matchesCat = selectedFoodCategory.equals("All", ignoreCase = true) || selectedFoodCategory.contains("All", ignoreCase = true) || menuItemStr.contains(selectedFoodCategory, ignoreCase = true)
                                    matchesQuery && matchesCat
                                }
                            } else {
                                emptyList()
                            }

                            if (filteredDishes.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No items added yet by owner.",
                                            fontSize = 14.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    filteredDishes.forEach { menuItemStr ->
                                        var itemName = menuItemStr
                                        var itemPrice = listing.price
                                        var itemPhoto = ""
                                        var itemDesc = "Delicious and fresh campus food prepared with quality ingredients."

                                        val parts = menuItemStr.split("|")
                                        if (parts.size >= 3) {
                                            itemName = parts[0].trim()
                                            val p2 = parts[1].trim()
                                            val p3 = parts[2].trim()

                                            if (p2.contains("data:image") || p2.contains("http") || p2.contains("content://")) {
                                                itemPhoto = p2
                                                itemDesc = p3
                                            } else {
                                                itemPrice = p2
                                                if (p3.contains("data:image") || p3.contains("http") || p3.contains("content://")) {
                                                    itemPhoto = p3
                                                } else {
                                                    itemDesc = p3
                                                }
                                            }
                                        } else if (parts.size == 2) {
                                            itemName = parts[0].trim()
                                            val second = parts[1].trim()
                                            if (second.contains("data:image") || second.contains("http") || second.contains("content://")) {
                                                itemPhoto = second
                                            } else {
                                                itemPrice = second
                                            }
                                        }

                                        var cleanItemName = itemName
                                        var extractedPrice = ""

                                        if (cleanItemName.contains("-")) {
                                            val dashParts = cleanItemName.split("-")
                                            val namePart = dashParts[0].trim()
                                            val numPart = dashParts[1].trim()
                                            if (numPart.matches(Regex("^[0-9.]+$")) || numPart.startsWith("₹")) {
                                                cleanItemName = namePart
                                                extractedPrice = if (numPart.startsWith("₹")) numPart else "₹$numPart"
                                            }
                                        }

                                        val displayPriceText = when {
                                            extractedPrice.isNotBlank() -> extractedPrice
                                            itemPrice.isNotBlank() && !itemPrice.contains("Open") && !itemPrice.contains("AM") && !itemPrice.contains("PM") -> {
                                                if (itemPrice.startsWith("₹")) itemPrice else "₹$itemPrice"
                                            }
                                            else -> "₹200"
                                        }

                                        val priceNum = parsePriceNumber(displayPriceText)

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (itemPhoto.isNotBlank()) {
                                                    ListingImage(
                                                        imageUrl = itemPhoto,
                                                        contentDescription = cleanItemName,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(75.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .clickable { expandedImageUrl = itemPhoto }
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(75.dp)
                                                            .clip(RoundedCornerShape(14.dp))
                                                            .background(Color(0xFFFCE4EC)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Restaurant,
                                                            contentDescription = null,
                                                            tint = Color(0xFFEC4899),
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = cleanItemName,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF1E1E2D)
                                                    )

                                                    Spacer(modifier = Modifier.height(2.dp))

                                                    Text(
                                                        text = itemDesc,
                                                        fontSize = 11.sp,
                                                        color = Color.Gray,
                                                        maxLines = 2,
                                                        lineHeight = 15.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Text(
                                                        text = displayPriceText,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFEC4899)
                                                    )
                                                }

                                                if (!isNoCartCategory) {
                                                    val existingCartItem = cart.find { it.name.equals(cleanItemName, ignoreCase = true) }

                                                    if (existingCartItem != null) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(20.dp))
                                                                .background(Color(0xFFFCE4EC))
                                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            IconButton(
                                                                onClick = {
                                                                    if (existingCartItem.quantity > 1) {
                                                                        existingCartItem.quantity--
                                                                    } else {
                                                                        cart.remove(existingCartItem)
                                                                    }
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Remove, contentDescription = "Remove", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                                            }

                                                            Text(
                                                                text = "${existingCartItem.quantity}",
                                                                fontWeight = FontWeight.ExtraBold,
                                                                fontSize = 14.sp,
                                                                color = Color(0xFFEC4899),
                                                                modifier = Modifier.padding(horizontal = 6.dp)
                                                            )

                                                            IconButton(
                                                                onClick = { existingCartItem.quantity++ },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = {
                                                                cart.add(
                                                                    LocalCartItem(
                                                                        name = cleanItemName,
                                                                        priceText = displayPriceText,
                                                                        priceNum = priceNum,
                                                                        photoUrl = itemPhoto
                                                                    )
                                                                )
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                            shape = RoundedCornerShape(12.dp),
                                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                                        ) {
                                                            Text("+ ADD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

                Box(modifier = Modifier.fillMaxWidth()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    )
                }
            }
        }
    }

    if (expandedImageUrl != null) {
        Dialog(onDismissRequest = { expandedImageUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .clickable { expandedImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                ListingImage(
                    imageUrl = expandedImageUrl!!,
                    contentDescription = "Room Photo Fullscreen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                IconButton(
                    onClick = { expandedImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    if (showCartDialog) {
        AlertDialog(
            onDismissRequest = { showCartDialog = false },
            title = {
                Text("Your Food Order Cart", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E2D))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    cart.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${item.quantity} x ${item.priceText}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("₹${(item.priceNum * item.quantity).toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFEC4899))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("₹${totalCartPrice.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCartDialog = false
                        showCheckoutDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Proceed to Order", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCartDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Text("Delivery Details", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1E2D))
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = custName,
                        onValueChange = { custName = it },
                        label = { Text("Your Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = custPhone,
                        onValueChange = { custPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = custPgName,
                        onValueChange = { custPgName = it },
                        label = { Text("PG / Hostel / Flat Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = custLocation,
                        onValueChange = { custLocation = it },
                        label = { Text("Delivery Address / Room No.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isFetchingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFEC4899))
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Detect Current GPS Location", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isBlank() || custPhone.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter Name and Phone Number") }
                            return@Button
                        }

                        showCheckoutDialog = false

                        val orderSb = StringBuilder()
                        orderSb.append("🍔 NEW CAMPUS ORDER PLACED!\n\n")
                        orderSb.append("Restaurant: ${listing.title}\n")
                        orderSb.append("Ordered Items:\n")

                        cart.forEach { item ->
                            val photoTag = if (item.photoUrl.isNotBlank()) " [IMAGE:${item.photoUrl}]" else ""
                            orderSb.append("• ${item.quantity}x ${item.name} (${item.priceText})$photoTag\n")
                        }

                        orderSb.append("\nGrand Total: ₹${totalCartPrice.toInt()}\n")
                        orderSb.append("Delivery Fee: ₹0 (FREE)\n\n")

                        orderSb.append("👤 Customer: $custName\n")
                        orderSb.append("📞 Phone: $custPhone\n")
                        if (custPgName.isNotBlank()) orderSb.append("🏠 PG/Hostel: $custPgName\n")
                        if (custLocation.isNotBlank()) orderSb.append("📍 Location: $custLocation\n")

                        if (listing.ownerUid.isNotBlank()) {
                            onSendOrderClick(listing.ownerUid, listing.title, listing.imageUrl, orderSb.toString())
                        }

                        cart.clear()
                        scope.launch { snackbarHostState.showSnackbar("Order Sent Successfully to ${listing.title}!") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Place Order Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCartDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
