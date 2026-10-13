package com.example.lovelyhub.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.R
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.chats.ChatDetailScreen
import com.example.lovelyhub.ui.chats.ChatListScreen
import com.example.lovelyhub.ui.chats.ChatViewModel
import com.example.lovelyhub.ui.listings.AddListingScreen
import com.example.lovelyhub.ui.listings.ListingDetailScreen
import com.example.lovelyhub.ui.listings.ListingListScreen
import com.example.lovelyhub.ui.listings.ListingViewModel
import com.example.lovelyhub.ui.profile.ProfileScreen
import com.example.lovelyhub.ui.theme.CatFavoritesColor
import com.example.lovelyhub.ui.theme.CatFavoritesGradient
import com.example.lovelyhub.ui.theme.CatJobsColor
import com.example.lovelyhub.ui.theme.CatJobsGradient
import com.example.lovelyhub.ui.theme.CatMarketplaceColor
import com.example.lovelyhub.ui.theme.CatMarketplaceGradient
import com.example.lovelyhub.ui.theme.CatRentalsColor
import com.example.lovelyhub.ui.theme.CatRentalsGradient
import com.example.lovelyhub.ui.theme.CatRestaurantColor
import com.example.lovelyhub.ui.theme.CatRestaurantGradient
import com.example.lovelyhub.ui.theme.CatRoomsColor
import com.example.lovelyhub.ui.theme.CatRoomsGradient
import com.example.lovelyhub.ui.theme.CatServicesColor
import com.example.lovelyhub.ui.theme.CatServicesGradient
import com.example.lovelyhub.ui.theme.LovelyAmber
import com.example.lovelyhub.ui.theme.LovelyBgLight
import com.example.lovelyhub.ui.theme.LovelyBorder
import com.example.lovelyhub.ui.theme.LovelyBottomNavBg
import com.example.lovelyhub.ui.theme.LovelyGreenOnline
import com.example.lovelyhub.ui.theme.LovelyHeaderGradient
import com.example.lovelyhub.ui.theme.LovelyNavActivePill
import com.example.lovelyhub.ui.theme.LovelyNavInactive
import com.example.lovelyhub.ui.theme.LovelyPurpleDark
import com.example.lovelyhub.ui.theme.LovelyPurpleLight
import com.example.lovelyhub.ui.theme.LovelyPurplePrimary
import com.example.lovelyhub.ui.theme.LovelyPurpleSoft
import com.example.lovelyhub.ui.theme.LovelyRedBadge
import com.example.lovelyhub.ui.theme.LovelyTextDark
import com.example.lovelyhub.ui.theme.LovelyTextMuted

data class HomeCategoryItem(
    val title: String,
    val cardResId: Int
)

data class ActiveChatTarget(
    val receiverUid: String,
    val receiverName: String,
    val receiverPhotoUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    listingViewModel: ListingViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    var selectedBottomNavIndex by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedListingDetail by remember { mutableStateOf<Listing?>(null) }
    var showAboutScreen by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeChatTarget by remember { mutableStateOf<ActiveChatTarget?>(null) }

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser

    val userPhotoUrl = userProfile?.photoUrl?.ifBlank { null } ?: currentUser?.photoUrl?.toString()
    val userName = userProfile?.name?.ifBlank { null } ?: currentUser?.displayName ?: "Ky Sr"

    BackHandler(enabled = activeChatTarget != null || showAboutScreen || selectedListingDetail != null || selectedCategory != null || selectedBottomNavIndex != 0) {
        when {
            activeChatTarget != null -> activeChatTarget = null
            showAboutScreen -> showAboutScreen = false
            selectedListingDetail != null -> selectedListingDetail = null
            selectedCategory != null -> selectedCategory = null
            selectedBottomNavIndex != 0 -> selectedBottomNavIndex = 0
        }
    }

    val categories = remember {
        listOf(
            HomeCategoryItem(title = "Restaurants", cardResId = R.drawable.card_restaurants),
            HomeCategoryItem(title = "Rooms", cardResId = R.drawable.card_rooms),
            HomeCategoryItem(title = "Rentals", cardResId = R.drawable.card_rentals),
            HomeCategoryItem(title = "Jobs", cardResId = R.drawable.card_jobs),
            HomeCategoryItem(title = "Marketplace", cardResId = R.drawable.card_marketplace),
            HomeCategoryItem(title = "Services", cardResId = R.drawable.card_services)
        )
    }

    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
    }

    if (showNotificationDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = LovelyPurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Notifications", fontWeight = FontWeight.Bold, color = LovelyTextDark)
                }
            },
            text = {
                Column {
                    Text(
                        text = "🎉 Welcome to LovelyHub!",
                        fontWeight = FontWeight.Bold,
                        color = LovelyTextDark,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find restaurants, rooms, jobs, and rentals around you with ease.",
                        color = LovelyTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💬 Active Chats",
                        fontWeight = FontWeight.Bold,
                        color = LovelyTextDark,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check the Chats tab to connect directly with sellers and students.",
                        color = LovelyTextMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationDialog = false }) {
                    Text(text = "OK", fontWeight = FontWeight.Bold, color = LovelyPurplePrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = LovelyPurplePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Quick Categories", fontWeight = FontWeight.Bold, color = LovelyTextDark)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(LovelyPurpleSoft)
                                .clickable {
                                    showFilterDialog = false
                                    selectedCategory = cat.title
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cat.title, fontWeight = FontWeight.Bold, color = LovelyTextDark)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(text = "Close", fontWeight = FontWeight.Bold, color = LovelyPurplePrimary)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = LovelyBottomNavBg,
                tonalElevation = 10.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            ) {
                val navItems = listOf(
                    "Home" to Icons.Default.Home,
                    "Add" to Icons.Default.AddBox,
                    "Chats" to Icons.AutoMirrored.Filled.Chat,
                    "Profile" to Icons.Default.Person
                )

                navItems.forEachIndexed { index, (label, icon) ->
                    val isSelected = activeChatTarget == null && !showAboutScreen && selectedBottomNavIndex == index && selectedCategory == null && selectedListingDetail == null
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            activeChatTarget = null
                            showAboutScreen = false
                            selectedBottomNavIndex = index
                            selectedCategory = null
                            selectedListingDetail = null
                        },
                        icon = {
                            Box {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(24.dp)
                                )
                                // Red notification dot on Chats
                                if (label == "Chats") {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = 2.dp, y = (-2).dp)
                                            .clip(CircleShape)
                                            .background(LovelyRedBadge)
                                    )
                                }
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = LovelyPurpleLight,
                            unselectedIconColor = LovelyNavInactive,
                            unselectedTextColor = LovelyNavInactive,
                            indicatorColor = LovelyNavActivePill
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = LovelyBgLight
        ) {
            when {
                activeChatTarget != null -> {
                    val target = activeChatTarget!!
                    ChatDetailScreen(
                        receiverUid = target.receiverUid,
                        receiverName = target.receiverName,
                        receiverPhotoUrl = target.receiverPhotoUrl,
                        viewModel = chatViewModel,
                        onBackClick = { activeChatTarget = null }
                    )
                }
                showAboutScreen -> {
                    AboutCompanyScreen(onBackClick = { showAboutScreen = false })
                }
                selectedListingDetail != null -> {
                    ListingDetailScreen(
                        listing = selectedListingDetail!!,
                        onBackClick = { selectedListingDetail = null },
                        onMessageClick = { receiverUid, receiverName, receiverPhotoUrl ->
                            activeChatTarget = ActiveChatTarget(receiverUid, receiverName, receiverPhotoUrl)
                        },
                        onSendOrderClick = { receiverUid, receiverName, receiverPhotoUrl, orderText ->
                            chatViewModel.sendMessage(receiverUid, orderText)
                            activeChatTarget = ActiveChatTarget(receiverUid, receiverName, receiverPhotoUrl)
                        }
                    )
                }
                selectedCategory != null -> {
                    ListingListScreen(
                        category = selectedCategory!!,
                        viewModel = listingViewModel,
                        onBackClick = { selectedCategory = null },
                        onListingClick = { listing -> selectedListingDetail = listing },
                        onAddListingClick = {
                            selectedCategory = null
                            selectedBottomNavIndex = 1
                        }
                    )
                }
                selectedBottomNavIndex == 1 -> {
                    AddListingScreen(
                        viewModel = listingViewModel,
                        authViewModel = viewModel,
                        onSuccess = { postedCategory ->
                            selectedCategory = postedCategory
                            selectedBottomNavIndex = 0
                        }
                    )
                }
                selectedBottomNavIndex == 2 -> {
                    ChatListScreen(
                        viewModel = chatViewModel,
                        onConversationClick = { conv ->
                            activeChatTarget = ActiveChatTarget(conv.otherUid, conv.otherName, conv.otherPhotoUrl)
                        }
                    )
                }
                selectedBottomNavIndex == 3 -> {
                    ProfileScreen(
                        viewModel = viewModel,
                        onSignOut = onSignOut
                    )
                }
                else -> {
                    // Modern Home Screen matching reference image
                    HomeDashboardContent(
                        userName = userName,
                        userPhotoUrl = userPhotoUrl,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        categories = filteredCategories,
                        onCategoryClick = { selectedCategory = it },
                        onProfileClick = { selectedBottomNavIndex = 3 },
                        onNotificationClick = { showNotificationDialog = true },
                        onFilterClick = { showFilterDialog = true },
                        onFavoritesClick = { selectedCategory = "Favorites" },
                        onAboutClick = { showAboutScreen = true }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeDashboardContent(
    userName: String,
    userPhotoUrl: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    categories: List<HomeCategoryItem>,
    onCategoryClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onFilterClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Curved Purple Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LovelyHeaderGradient)
                .padding(top = statusBarPadding + 8.dp, start = 18.dp, end = 18.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LovelyHub Brand Header Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onAboutClick() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.header_brand_logo),
                        contentDescription = "LovelyHub Logo",
                        modifier = Modifier.height(44.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Notification Bell with Red Badge Dot
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.45f))
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    // Red Notification Badge Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(LovelyRedBadge)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // User Profile Avatar with Golden Glow Border
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, LovelyAmber, CircleShape)
                        .clickable { onProfileClick() }
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_user_avatar),
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Body Content with Overlapping Welcome Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-14).dp)
                .padding(horizontal = 18.dp)
        ) {
            // Elevated Welcome Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with Green Online Dot
                    Box(modifier = Modifier.size(56.dp)) {
                        if (userPhotoUrl != null) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, LovelyPurplePrimary, CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_user_avatar),
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(2.dp, LovelyPurplePrimary, CircleShape)
                            )
                        }

                        // Online green dot badge
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(LovelyGreenOnline)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Welcome",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LovelyPurplePrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "👋", fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LovelyTextDark
                        )
                    }

                    // Circular Chevron Right Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LovelyPurpleSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open Profile",
                            tint = LovelyPurplePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar & Filter Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White)
                        .border(1.2.dp, LovelyBorder, RoundedCornerShape(28.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = LovelyTextMuted,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search here...",
                                    color = LovelyTextMuted,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = LovelyTextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(LovelyPurplePrimary),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = LovelyTextMuted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onSearchQueryChange("") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Filter / Tune Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.2.dp, LovelyBorder, RoundedCornerShape(18.dp))
                        .clickable { onFilterClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = LovelyPurplePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2-Column Category Grid
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val chunked = categories.chunked(2)
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowItems.forEach { cat ->
                            CategoryCard(
                                category = cat,
                                modifier = Modifier.weight(1f),
                                onClick = { onCategoryClick(cat.title) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Favorites Banner Card
            FavoritesBannerCard(onClick = onFavoritesClick)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryCard(
    category: HomeCategoryItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(633f / 432f)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            painter = painterResource(id = category.cardResId),
            contentDescription = category.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun FavoritesBannerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1308f / 240f)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.card_favorites),
            contentDescription = "Favorites",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun AboutCompanyScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LovelyBgLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(LovelyPurpleSoft)
                .clickable { onBackClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Back",
                tint = LovelyPurplePrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Back to Home",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = LovelyPurplePrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .border(3.5.dp, LovelyPurplePrimary, CircleShape)
                .background(LovelyPurpleSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LovelyPurplePrimary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Kaisar Ahmad Lone",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LovelyTextDark
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Android Developer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = LovelyPurplePrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Developer of LovelyHub App",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = LovelyTextDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "If you want any custom mobile application, website, or enterprise software solution developed, feel free to contact me.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = LovelyTextMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = LovelyBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:lonekaisar15@gmail.com"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = LovelyPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "lonekaisar15@gmail.com",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LovelyTextDark
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9086455406"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = LovelyPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "9086455406",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LovelyTextDark
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KaisarAhLone"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Link, contentDescription = null, tint = LovelyPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "GitHub Profile",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LovelyTextDark
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/kaisar-ah-lone-/"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Link, contentDescription = null, tint = LovelyPurplePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "LinkedIn Profile",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LovelyTextDark
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = LovelyBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "View Portfolio",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LovelyPurplePrimary,
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KaisarAhLone"))
                try { context.startActivity(intent) } catch (_: Exception) {}
            }
        )
    }
}
