package com.example.lovelyhub.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.R
import com.example.lovelyhub.data.local.LocalUserCache
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
import com.example.lovelyhub.ui.theme.CatJobsColor
import com.example.lovelyhub.ui.theme.CatMarketplaceColor
import com.example.lovelyhub.ui.theme.CatRentalsColor
import com.example.lovelyhub.ui.theme.CatRestaurantColor
import com.example.lovelyhub.ui.theme.CatRoomsColor
import com.example.lovelyhub.ui.theme.CatServicesColor
import com.example.lovelyhub.ui.theme.LovelyAmber
import com.example.lovelyhub.ui.theme.LovelyBgLight
import com.example.lovelyhub.ui.theme.LovelyBorder
import com.example.lovelyhub.ui.theme.LovelyBottomNavBg
import com.example.lovelyhub.ui.theme.LovelyGreenOnline
import com.example.lovelyhub.ui.theme.LovelyHeaderGradient
import com.example.lovelyhub.ui.theme.LovelyNavActivePill
import com.example.lovelyhub.ui.theme.LovelyNavInactive
import com.example.lovelyhub.ui.theme.LovelyPurpleLight
import com.example.lovelyhub.ui.theme.LovelyPurplePrimary
import com.example.lovelyhub.ui.theme.LovelyPurpleSoft
import com.example.lovelyhub.ui.theme.LovelyRedBadge
import com.example.lovelyhub.ui.theme.LovelyTextDark
import com.example.lovelyhub.ui.theme.LovelyTextMuted

data class ActiveChatTarget(
    val receiverUid: String,
    val receiverName: String,
    val receiverPhotoUrl: String = ""
)

data class HomeCategoryItem(
    val title: String,
    val bgResId: Int,
    val icon: ImageVector,
    val pillColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    listingViewModel: ListingViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var selectedBottomNavIndex by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedListingDetail by remember { mutableStateOf<Listing?>(null) }
    var showAboutScreen by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var activeChatTarget by remember { mutableStateOf<ActiveChatTarget?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser
    val localUserCache = remember { LocalUserCache(context) }

    val cachedLocalPhoto = remember(currentUser?.uid, userProfile?.photoUrl) {
        localUserCache.getProfilePhoto(currentUser?.uid ?: "")
    }

    val userPhotoUrl = userProfile?.photoUrl?.ifBlank { null }
        ?: cachedLocalPhoto
        ?: currentUser?.photoUrl?.toString()
    val userName = userProfile?.name?.ifBlank { null } ?: currentUser?.displayName ?: "Ky Sr"

    LaunchedEffect(currentUser) {
        chatViewModel.initLocalCache(context)
        currentUser?.uid?.let { uid ->
            viewModel.loadUserProfile(uid, context)
        }
    }

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
            HomeCategoryItem(
                title = "Restaurants",
                bgResId = R.drawable.bg_card_restaurants,
                icon = Icons.Default.Restaurant,
                pillColor = CatRestaurantColor
            ),
            HomeCategoryItem(
                title = "Rooms",
                bgResId = R.drawable.bg_card_rooms,
                icon = Icons.Default.Home,
                pillColor = CatRoomsColor
            ),
            HomeCategoryItem(
                title = "Rentals",
                bgResId = R.drawable.bg_card_rentals,
                icon = Icons.Default.DirectionsCar,
                pillColor = CatRentalsColor
            ),
            HomeCategoryItem(
                title = "Jobs",
                bgResId = R.drawable.bg_card_jobs,
                icon = Icons.Default.Work,
                pillColor = CatJobsColor
            ),
            HomeCategoryItem(
                title = "Marketplace",
                bgResId = R.drawable.bg_card_marketplace,
                icon = Icons.Default.Storefront,
                pillColor = CatMarketplaceColor
            ),
            HomeCategoryItem(
                title = "Services",
                bgResId = R.drawable.bg_card_services,
                icon = Icons.Default.Build,
                pillColor = CatServicesColor
            )
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
                        text = "Find restaurants, rooms, jobs, rentals, marketplace & services around you.",
                        color = LovelyTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💬 Offline Chat Ready",
                        fontWeight = FontWeight.Bold,
                        color = LovelyTextDark,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your messages and chats are cached locally on phone for instant speed.",
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
                                .background(cat.pillColor.copy(alpha = 0.12f))
                                .clickable {
                                    showFilterDialog = false
                                    selectedCategory = cat.title
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(cat.icon, contentDescription = null, tint = cat.pillColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
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
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = LovelyBottomNavBg,
                tonalElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
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
                .padding(bottom = innerPadding.calculateBottomPadding()),
            color = LovelyBgLight
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                            onListingClick = { listing ->
                                if (!listing.category.contains("Services", ignoreCase = true)) {
                                    selectedListingDetail = listing
                                }
                            },
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
                            onSignOut = {
                                chatViewModel.clearOnSignOut()
                                val uid = currentUser?.uid ?: ""
                                if (uid.isNotBlank()) {
                                    localUserCache.removeProfilePhoto(uid)
                                }
                                viewModel.signOut()
                                onSignOut()
                            }
                        )
                    }
                    else -> {
                        MainHomeFeedContent(
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
}

@Composable
fun MainHomeFeedContent(
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner_top_lovelyhub),
                contentDescription = "LovelyHub Top Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1024f / 341f),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, start = 14.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .width(130.dp)
                        .clickable { onAboutClick() }
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.38f))
                        .clickable { onNotificationClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-6).dp, y = 6.dp)
                            .clip(CircleShape)
                            .background(LovelyRedBadge)
                            .border(1.2.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { onAboutClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About Developer",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "About",
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
                .offset(y = (-14).dp)
                .padding(horizontal = 18.dp)
        ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            .aspectRatio(1.52f)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = category.bgResId),
                contentDescription = category.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(category.pillColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = category.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1B4B)
                    )
                }

                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = category.pillColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesBannerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(822f / 230f)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.bg_card_favorites),
                contentDescription = "Favorites",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(CatFavoritesColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Favorites",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1B4B)
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = CatFavoritesColor,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutCompanyScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val pinkHeaderGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFEC4899), Color(0xFFDB2777))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF2F8))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(pinkHeaderGradient)
                .padding(top = 12.dp, bottom = 45.dp, start = 14.dp, end = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "About Developer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-30).dp)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.5.dp, Color.White, CircleShape)
                    .shadow(elevation = 6.dp, shape = CircleShape)
            ) {
                AsyncImage(
                    model = "https://github.com/KaisarAhLone.png",
                    contentDescription = "Kaisar Ahmad Lone",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Kaisar Ahmad Lone",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1E2D)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFFF8E1))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Android Developer",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Developer of LovelyHub app",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "If you want any custom mobile application, website, or enterprise software solution developed, feel free to contact me.",
                fontSize = 13.sp,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium,
                lineHeight = 19.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Email", "lonekaisar15@gmail.com")
                        clipboard.setPrimaryClip(clip)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE4EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Email", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text("lonekaisar15@gmail.com", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Email", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Phone", "9086455406")
                        clipboard.setPrimaryClip(clip)
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE4EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Phone", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text("9086455406", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E2D))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Phone", tint = Color(0xFFEC4899), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1E2D)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:lonekaisar15@gmail.com"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Send Email", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:9086455406"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
