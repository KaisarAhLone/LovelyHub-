package com.example.lovelyhub.ui.listings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.components.MapLocationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class ServiceCategoryItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTileBg: Color,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingListScreen(
    category: String,
    viewModel: ListingViewModel,
    onBackClick: () -> Unit,
    onListingClick: (Listing) -> Unit,
    onAddListingClick: () -> Unit
) {
    val context = LocalContext.current
    val listingState by viewModel.listingState.collectAsState()
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var listingToDelete by remember { mutableStateOf<Listing?>(null) }
    var serviceSearchQuery by remember { mutableStateOf("") }
    var providerSearchQuery by remember { mutableStateOf("") }
    var selectedServiceFilter by remember { mutableStateOf<String?>(null) }
    var selectedFavTab by remember { mutableStateOf("All") }
    var selectedJobFilter by remember { mutableStateOf("All Jobs") }

    var showAddServiceDialog by remember { mutableStateOf(false) }
    var newServiceName by remember { mutableStateOf("") }
    var newServiceSubtitle by remember { mutableStateOf("") }
    var newServicePhone by remember { mutableStateOf("") }
    var newServiceWhatsApp by remember { mutableStateOf("") }
    var newServiceLocation by remember { mutableStateOf("") }
    var isSubmittingService by remember { mutableStateOf(false) }

    val isServicesCategory = category.equals("Services", ignoreCase = true)
    val isFavoritesCategory = category.equals("Favorites", ignoreCase = true)
    val isJobsCategory = category.equals("Jobs", ignoreCase = true)
    val isMarketplaceCategory = category.equals("Marketplace", ignoreCase = true)

    val favCategories = listOf("All", "Restaurants", "Rooms", "Rentals", "Jobs", "Marketplace", "Services")
    val jobFilterCategories = listOf("All Jobs", "Part-time", "Full-time", "On Campus", "Remote")

    val serviceCategories = remember {
        listOf(
            ServiceCategoryItem("Laundry Services", "Washing, ironing & dry cleaning", Icons.Default.LocalLaundryService, Color(0xFFEDE7F6), Color(0xFF7E57C2)),
            ServiceCategoryItem("Room Cleaning", "Hostel, PG & flat cleaning", Icons.Default.CleaningServices, Color(0xFFE0F2F1), Color(0xFF26A69A)),
            ServiceCategoryItem("Electrician", "Electrical repairs & installation", Icons.Default.ElectricalServices, Color(0xFFFFF8E1), Color(0xFFFFA726)),
            ServiceCategoryItem("Plumber", "Pipes, taps & water leakage", Icons.Default.Plumbing, Color(0xFFE3F2FD), Color(0xFF42A5F5)),
            ServiceCategoryItem("Laptop & Mobile Repair", "Device servicing & repairs", Icons.Default.Computer, Color(0xFFEDE7F6), Color(0xFF5C6BC0)),
            ServiceCategoryItem("AC & Appliance Repair", "AC, fridge & washing machine", Icons.Default.AcUnit, Color(0xFFE0F7FA), Color(0xFF26C6DA)),
            ServiceCategoryItem("Salon & Grooming", "Haircut, beard & beauty services", Icons.Default.ContentCut, Color(0xFFFCE4EC), Color(0xFFEC407A)),
            ServiceCategoryItem("Bike & Cycle Repair", "Puncture & servicing", Icons.Default.PedalBike, Color(0xFFFFF3E0), Color(0xFFFF7043)),
            ServiceCategoryItem("Home Tuition", "Tutoring & exam preparation", Icons.AutoMirrored.Filled.MenuBook, Color(0xFFE8F5E9), Color(0xFF66BB6A)),
            ServiceCategoryItem("Shifting & Packing", "Room shifting & luggage transport", Icons.Default.LocalShipping, Color(0xFFFFEBEE), Color(0xFFEF5350)),
            ServiceCategoryItem("Photography", "Events, portraits & birthdays", Icons.Default.PhotoCamera, Color(0xFFEDE7F6), Color(0xFF7E57C2)),
            ServiceCategoryItem("Water Can Delivery", "Drinking water for hostels & PGs", Icons.Default.WaterDrop, Color(0xFFE3F2FD), Color(0xFF29B6F6)),
            ServiceCategoryItem("Local Delivery", "Parcel pickup & drop", Icons.Default.LocalShipping, Color(0xFFE8F5E9), Color(0xFF4CAF50)),
            ServiceCategoryItem("Car Wash & Repair", "Cleaning & basic servicing", Icons.Default.DirectionsCar, Color(0xFFFFF8E1), Color(0xFFFFB300)),
            ServiceCategoryItem("Wi-Fi & Internet Setup", "Router setup & connectivity", Icons.Default.Wifi, Color(0xFFEDE7F6), Color(0xFF5C6BC0))
        )
    }

    val topHeaderGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF1E1E2D),
        unfocusedTextColor = Color(0xFF1E1E2D),
        focusedLabelColor = Color(0xFFEC4899),
        unfocusedLabelColor = Color(0xFFEC4899),
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        focusedLeadingIconColor = Color(0xFFEC4899),
        unfocusedLeadingIconColor = Color(0xFFEC4899),
        focusedBorderColor = Color(0xFFEC4899),
        unfocusedBorderColor = Color.LightGray,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    LaunchedEffect(category) {
        viewModel.fetchListings(category)
    }

    fun openWhatsApp(targetNum: String, serviceTitle: String) {
        val cleanNum = targetNum.replace(Regex("[^0-9]"), "")
        val fullNum = if (!cleanNum.startsWith("91") && cleanNum.length == 10) "91$cleanNum" else cleanNum

        val msg = "Hello! I found your $serviceTitle on LovelyHub. I would like to inquire about your item/service."
        val encodedMsg = Uri.encode(msg)

        val waUri = Uri.parse("https:" + "/" + "/api.whatsapp.com/send?phone=$fullNum&text=$encodedMsg")
        val intent = Intent(Intent.ACTION_VIEW, waUri)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            val fallbackUri = Uri.parse("https:" + "/" + "/wa.me/$fullNum?text=$encodedMsg")
            val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
            try { context.startActivity(fallbackIntent) } catch (_: Exception) {}
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
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
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(topHeaderGradient)
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
                                        if (selectedServiceFilter != null) {
                                            selectedServiceFilter = null
                                        } else {
                                            onBackClick()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Column {
                                    Text(
                                        text = if (selectedServiceFilter != null) selectedServiceFilter!! else category,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isMarketplaceCategory) "Buy and sell items near your campus" else (if (isFavoritesCategory) "All your saved items & services in one place" else (if (isJobsCategory) "Find trusted jobs near your campus" else (if (selectedServiceFilter != null) "Find service providers near you" else "Find trusted ${category.lowercase()} near your campus"))),
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    if (isFavoritesCategory) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favCategories) { favCat ->
                                val isSel = selectedFavTab.equals(favCat, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSel) Color(0xFFEC4899) else Color(0xFFF0F1F5))
                                        .clickable { selectedFavTab = favCat }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = favCat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF1E1E2D)
                                    )
                                }
                            }
                        }
                    }

                    if (isServicesCategory && selectedServiceFilter == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = serviceSearchQuery,
                                onValueChange = { serviceSearchQuery = it },
                                placeholder = { Text("Search services...", color = Color.Gray, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val filteredServices = serviceCategories.filter {
                                serviceSearchQuery.isBlank() || it.title.contains(serviceSearchQuery, ignoreCase = true) || it.subtitle.contains(serviceSearchQuery, ignoreCase = true)
                            }

                            filteredServices.forEach { serv ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable { selectedServiceFilter = serv.title },
                                    shape = RoundedCornerShape(18.dp),
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(serv.iconTileBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = serv.icon,
                                                    contentDescription = serv.title,
                                                    tint = serv.iconColor,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Text(
                                                    text = serv.title,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF1E1E2D)
                                                )

                                                Spacer(modifier = Modifier.height(2.dp))

                                                Text(
                                                    text = serv.subtitle,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
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
                            }
                        }
                    } else if (isServicesCategory && selectedServiceFilter != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = providerSearchQuery,
                                onValueChange = { providerSearchQuery = it },
                                placeholder = { Text("Search service providers...", color = Color.Gray, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val servObj = serviceCategories.find { it.title.equals(selectedServiceFilter, ignoreCase = true) }
                                    newServiceName = ""
                                    newServiceSubtitle = servObj?.subtitle ?: ""
                                    newServicePhone = ""
                                    newServiceWhatsApp = ""
                                    newServiceLocation = "Phagwara, Punjab"
                                    showAddServiceDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Add Your ${selectedServiceFilter!!}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val matchedCategoryObj = serviceCategories.find { it.title.equals(selectedServiceFilter, ignoreCase = true) }

                            when (val state = listingState) {
                                is ListingState.Loading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFFEC4899))
                                    }
                                }
                                is ListingState.Error -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = state.message, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                is ListingState.Success -> {
                                    val providers = state.items.filter { provider ->
                                        val filterKey = selectedServiceFilter!!.trim()

                                        val isThisSubService = provider.category.equals("Services|$filterKey", ignoreCase = true) ||
                                                provider.category.endsWith("|$filterKey", ignoreCase = true) ||
                                                provider.category.equals(filterKey, ignoreCase = true) ||
                                                (provider.category.equals("Services", ignoreCase = true) &&
                                                        (provider.menuItems.contains(filterKey) || provider.price.equals(filterKey, ignoreCase = true)))

                                        val matchesQuery = providerSearchQuery.isBlank() ||
                                                provider.title.contains(providerSearchQuery, ignoreCase = true) ||
                                                provider.location.contains(providerSearchQuery, ignoreCase = true) ||
                                                provider.price.contains(providerSearchQuery, ignoreCase = true)

                                        isThisSubService && matchesQuery
                                    }

                                    if (providers.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "No providers listed for ${selectedServiceFilter!!} yet!",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = {
                                                        showAddServiceDialog = true
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Be First to Add Your Service", fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(bottom = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            items(providers) { provider ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(20.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(56.dp)
                                                                    .clip(RoundedCornerShape(16.dp))
                                                                    .background(matchedCategoryObj?.iconTileBg ?: Color(0xFFEDE7F6)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = matchedCategoryObj?.icon ?: Icons.Default.Build,
                                                                    contentDescription = null,
                                                                    tint = matchedCategoryObj?.iconColor ?: Color(0xFFEC4899),
                                                                    modifier = Modifier.size(28.dp)
                                                                )
                                                            }

                                                            Spacer(modifier = Modifier.width(14.dp))

                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                    text = provider.title,
                                                                    fontSize = 17.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = Color(0xFF1E1E2D)
                                                                )

                                                                Spacer(modifier = Modifier.height(2.dp))

                                                                Text(
                                                                    text = if (provider.price.isNotBlank() && !provider.price.startsWith("₹") && !provider.price.equals(selectedServiceFilter, ignoreCase = true)) provider.price else (matchedCategoryObj?.subtitle ?: selectedServiceFilter!!),
                                                                    fontSize = 13.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = Color.Gray
                                                                )

                                                                Spacer(modifier = Modifier.height(2.dp))

                                                                Text(
                                                                    text = provider.location.ifBlank { "Phagwara, Punjab" },
                                                                    fontSize = 12.sp,
                                                                    color = Color.DarkGray
                                                                )
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(14.dp))
                                                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                                                        Spacer(modifier = Modifier.height(14.dp))

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    if (provider.phone.isNotBlank()) {
                                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .weight(1f)
                                                                    .height(42.dp),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFFEC4899)),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }

                                                            Button(
                                                                onClick = {
                                                                    val num = provider.whatsApp.ifBlank { provider.phone }
                                                                    if (num.isNotBlank()) {
                                                                        openWhatsApp(num, selectedServiceFilter!!)
                                                                    }
                                                                },
                                                                modifier = Modifier
                                                                    .weight(1.1f)
                                                                    .height(42.dp),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            }

                                                            Button(
                                                                onClick = {
                                                                    MapLocationHelper.openMapDirections(context, provider.location)
                                                                },
                                                                modifier = Modifier
                                                                    .weight(1.1f)
                                                                    .height(42.dp),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1976D2)),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (val state = listingState) {
                                is ListingState.Loading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color(0xFFEC4899))
                                    }
                                }
                                is ListingState.Error -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(text = state.message, color = Color.DarkGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                is ListingState.Success -> {
                                    val items = if (isFavoritesCategory) {
                                        val allFavs = state.items
                                        if (selectedFavTab.equals("All", ignoreCase = true)) {
                                            allFavs
                                        } else {
                                            allFavs.filter {
                                                it.category.contains(selectedFavTab, ignoreCase = true) ||
                                                        selectedFavTab.contains(it.category, ignoreCase = true) ||
                                                        it.price.contains(selectedFavTab, ignoreCase = true) ||
                                                        it.title.contains(selectedFavTab, ignoreCase = true)
                                            }
                                        }
                                    } else {
                                        state.items
                                    }

                                    if (items.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = if (isFavoritesCategory) "No saved favorites in $selectedFavTab yet!" else "No listings in $category yet!",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(14.dp))
                                                if (!isFavoritesCategory) {
                                                    Button(
                                                        onClick = onAddListingClick,
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text("Add First Listing in $category", fontWeight = FontWeight.Bold, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedTextField(
                                                        value = serviceSearchQuery,
                                                        onValueChange = { serviceSearchQuery = it },
                                                        placeholder = {
                                                            Text(
                                                                text = if (isJobsCategory) "Search jobs (e.g. store, part-time, LPU...)" else (if (isMarketplaceCategory) "Search items (e.g. phone, laptop, books...)" else "Search ${category.lowercase()}..."),
                                                                color = Color.Gray,
                                                                fontSize = 13.sp
                                                            )
                                                        },
                                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(24.dp),
                                                        colors = fieldColors,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(Color.White)
                                                            .border(1.2.dp, Color(0xFFFCE4EC), RoundedCornerShape(16.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }

                                            if (isJobsCategory) {
                                                item {
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        items(jobFilterCategories) { jobCat ->
                                                            val isSel = selectedJobFilter.equals(jobCat, ignoreCase = true)
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(18.dp))
                                                                    .background(if (isSel) Color(0xFFEC4899) else Color.White)
                                                                    .border(1.dp, if (isSel) Color(0xFFEC4899) else Color(0xFFFCE4EC), RoundedCornerShape(18.dp))
                                                                    .clickable { selectedJobFilter = jobCat }
                                                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                                                            ) {
                                                                Text(
                                                                    text = jobCat,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (isSel) Color.White else Color(0xFF1E1E2D)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            val searchFilteredItems = items.filter { item ->
                                                val matchesSearch = serviceSearchQuery.isBlank() ||
                                                        item.title.contains(serviceSearchQuery, ignoreCase = true) ||
                                                        item.location.contains(serviceSearchQuery, ignoreCase = true) ||
                                                        item.price.contains(serviceSearchQuery, ignoreCase = true)

                                                val matchesJobFilter = !isJobsCategory || selectedJobFilter.equals("All Jobs", ignoreCase = true) ||
                                                        item.price.contains(selectedJobFilter, ignoreCase = true) ||
                                                        item.title.contains(selectedJobFilter, ignoreCase = true)

                                                matchesSearch && matchesJobFilter
                                            }

                                            items(searchFilteredItems) { listing ->
                                                ListingCardItem(
                                                    listing = listing,
                                                    isOwner = currentUid.isNotBlank() && listing.ownerUid == currentUid,
                                                    onClick = { onListingClick(listing) },
                                                    onCallClick = {
                                                        if (listing.phone.isNotBlank()) {
                                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                                            context.startActivity(intent)
                                                        }
                                                    },
                                                    onWhatsAppClick = {
                                                        val num = listing.whatsApp.ifBlank { listing.phone }
                                                        if (num.isNotBlank()) {
                                                            openWhatsApp(num, listing.title)
                                                        }
                                                    },
                                                    onFavoriteToggle = {
                                                        viewModel.toggleFavorite(listing.id, listing.isFavorite, category)
                                                    },
                                                    onDeleteClick = {
                                                        listingToDelete = listing
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddServiceDialog && selectedServiceFilter != null) {
        AlertDialog(
            onDismissRequest = { if (!isSubmittingService) showAddServiceDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFEC4899))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add ${selectedServiceFilter!!}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newServiceName,
                        onValueChange = { newServiceName = it },
                        label = { Text("Service / Business Name", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("e.g. Quick Wash Laundry") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFEC4899)) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newServiceSubtitle,
                        onValueChange = { newServiceSubtitle = it },
                        label = { Text("Specialization / Subtitle", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("e.g. Laundry & Ironing") },
                        leadingIcon = { Icon(Icons.Default.Title, contentDescription = null, tint = Color(0xFFEC4899)) },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newServicePhone,
                        onValueChange = { newServicePhone = it },
                        label = { Text("Call Phone Number", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFEC4899)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newServiceWhatsApp,
                        onValueChange = { newServiceWhatsApp = it },
                        label = { Text("WhatsApp Number", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null, tint = Color(0xFF2E7D32)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newServiceLocation,
                        onValueChange = { newServiceLocation = it },
                        label = { Text("Location / Address", fontWeight = FontWeight.Bold) },
                        placeholder = { Text("e.g. Phagwara, Punjab") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899)) },
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
                        if (newServiceName.isBlank() || newServicePhone.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter Service Name & Phone Number") }
                            return@Button
                        }

                        isSubmittingService = true
                        val targetSubService = selectedServiceFilter!!.trim()
                        val newServiceListing = Listing(
                            category = "Services|$targetSubService",
                            title = newServiceName.trim(),
                            price = if (newServiceSubtitle.isNotBlank()) newServiceSubtitle.trim() else targetSubService,
                            phone = newServicePhone.trim(),
                            whatsApp = if (newServiceWhatsApp.isNotBlank()) newServiceWhatsApp.trim() else newServicePhone.trim(),
                            location = if (newServiceLocation.isNotBlank()) newServiceLocation.trim() else "Phagwara, Punjab",
                            menuItems = listOf(targetSubService)
                        )

                        viewModel.postListing(context, newServiceListing, null) { error ->
                            isSubmittingService = false
                            if (error == null) {
                                showAddServiceDialog = false
                                newServiceName = ""
                                newServiceSubtitle = ""
                                newServicePhone = ""
                                newServiceWhatsApp = ""
                                newServiceLocation = ""
                                viewModel.fetchListings("Services")
                                scope.launch { snackbarHostState.showSnackbar("Service added successfully!") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            }
                        }
                    },
                    enabled = !isSubmittingService,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                ) {
                    if (isSubmittingService) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text("Add Service Now", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddServiceDialog = false },
                    enabled = !isSubmittingService
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (listingToDelete != null) {
        AlertDialog(
            onDismissRequest = { listingToDelete = null },
            title = { Text("Delete / Mark as Sold", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${listingToDelete?.title}'? It will be removed permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        val item = listingToDelete
                        listingToDelete = null
                        if (item != null) {
                            viewModel.deleteListing(item.id, category) {
                                scope.launch { snackbarHostState.showSnackbar("Listing deleted successfully!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete Now")
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

@Composable
fun ListingCardItem(
    listing: Listing,
    isOwner: Boolean,
    onClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val isJob = listing.category.equals("Jobs", ignoreCase = true)
    val isMarketplace = listing.category.equals("Marketplace", ignoreCase = true)

    fun cleanLocationName(rawLocation: String): String {
        if (rawLocation.isBlank()) return "LPU Campus"
        val idx = rawLocation.indexOf("(")
        return if (idx != -1) rawLocation.substring(0, idx).trim() else rawLocation.trim()
    }

    val displayLocation = cleanLocationName(listing.location)

    if (isMarketplace) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    if (listing.imageUrl.isNotBlank()) {
                        val coverPhotos = listing.imageUrl.split("~").filter { it.isNotBlank() }
                        ListingImage(
                            imageUrl = coverPhotos.firstOrNull() ?: listing.imageUrl,
                            contentDescription = listing.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("📷 1/${coverPhotos.size.coerceAtLeast(1)}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
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
                                modifier = Modifier.size(42.dp)
                            )
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
                        Text(
                            text = listing.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1E2D),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        val formattedPrice = if (listing.price.startsWith("₹")) listing.price else "₹ ${listing.price}"
                        Text(
                            text = formattedPrice,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEC4899)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE4EC))
                                .clickable { onFavoriteToggle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (listing.isFavorite) Color(0xFFFF1744) else Color(0xFFEC4899),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayLocation,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE4EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(13.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Campus Seller", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E2D))
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(listing.rating.ifBlank { "4.8" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onWhatsAppClick()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE4EC), contentColor = Color(0xFFEC4899)),
                            border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onClick,
                            modifier = Modifier.weight(1.2f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("View Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (isJob) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        if (listing.imageUrl.isNotBlank()) {
                            val coverPhotos = listing.imageUrl.split("~").filter { it.isNotBlank() }
                            ListingImage(
                                imageUrl = coverPhotos.firstOrNull() ?: listing.imageUrl,
                                contentDescription = listing.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFEDE7F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = null,
                                    tint = Color(0xFF6D28D9),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = listing.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1E2D),
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable { onFavoriteToggle() },
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

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = displayLocation,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEDE7F6))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Work, contentDescription = null, tint = Color(0xFF6D28D9), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (listing.price.contains("Full", ignoreCase = true)) "Full-time" else "Part-time",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6D28D9)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Flexible hours",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFCE4EC))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val is24Job = listing.is24Hours || listing.price.contains("24 Hours", ignoreCase = true) || listing.openTime.contains("24 Hours", ignoreCase = true) || listing.price.contains("24/7", ignoreCase = true)
                            Text(
                                text = if (is24Job) "Open 24 Hours • Campus Job" else "Open: ${listing.openTime} - ${listing.closeTime}",
                                color = Color(0xFF1E1E2D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = onWhatsAppClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                MapLocationHelper.openMapDirections(context, listing.location)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (listing.imageUrl.isNotBlank()) {
                            val coverPhotos = listing.imageUrl.split("~").filter { it.isNotBlank() }
                            ListingImage(
                                imageUrl = coverPhotos.firstOrNull() ?: listing.imageUrl,
                                contentDescription = listing.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val icon = when (listing.category) {
                                "Restaurants", "Food" -> Icons.Default.Restaurant
                                "Rooms" -> Icons.Default.HomeWork
                                "Rentals" -> Icons.Default.DirectionsCar
                                "Jobs" -> Icons.Default.Work
                                "Marketplace" -> Icons.Default.ShoppingBag
                                else -> Icons.Default.Storefront
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFFCE4EC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color(0xFFEC4899),
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onFavoriteToggle() },
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = listing.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1E2D)
                            )

                            if (displayLocation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = displayLocation,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onCallClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = onWhatsAppClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                MapLocationHelper.openMapDirections(context, listing.location)
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Directions", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFCE4EC))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val is24Service = listing.is24Hours || listing.price.contains("24 Hours", ignoreCase = true) || listing.openTime.contains("24 Hours", ignoreCase = true) || listing.price.contains("24/7", ignoreCase = true)
                            Text(
                                text = if (is24Service) "Open 24 Hours • Free Service" else "Open: ${listing.openTime} - ${listing.closeTime}",
                                color = Color(0xFF1E1E2D),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
