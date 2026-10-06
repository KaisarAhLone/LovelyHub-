package com.example.lovelyhub.ui.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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

data class HomeCategoryItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
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
    var showProfileScreen by remember { mutableStateOf(false) }
    var activeChatTarget by remember { mutableStateOf<ActiveChatTarget?>(null) }

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser

    val userPhotoUrl = userProfile?.photoUrl?.ifBlank { null } ?: currentUser?.photoUrl?.toString()
    val userName = userProfile?.name?.ifBlank { null } ?: currentUser?.displayName ?: "Campus Student"

    BackHandler(enabled = activeChatTarget != null || showProfileScreen || selectedListingDetail != null || selectedCategory != null || selectedBottomNavIndex != 0) {
        when {
            activeChatTarget != null -> {
                activeChatTarget = null
            }
            showProfileScreen -> {
                showProfileScreen = false
            }
            selectedListingDetail != null -> {
                selectedListingDetail = null
            }
            selectedCategory != null -> {
                selectedCategory = null
            }
            selectedBottomNavIndex != 0 -> {
                selectedBottomNavIndex = 0
            }
        }
    }

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFFFFB300)
        )
    )

    val categories = remember {
        listOf(
            HomeCategoryItem("Restaurants", Icons.Default.Restaurant, Color(0xFFFF8F00), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Rooms", Icons.Default.HomeWork, Color(0xFF0288D1), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Rentals", Icons.Default.DirectionsCar, Color(0xFF1565C0), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Jobs", Icons.Default.Work, Color(0xFF7B1FA2), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Marketplace", Icons.Default.Storefront, Color(0xFFC2185B), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Services", Icons.Default.Build, Color(0xFF2E7D32), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Open Now", Icons.Default.Schedule, Color(0xFF00796B), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("Favorites", Icons.Default.Favorite, Color(0xFFD32F2F), Color.White.copy(alpha = 0.95f)),
            HomeCategoryItem("More", Icons.Default.MoreHoriz, Color(0xFF512DA8), Color.White.copy(alpha = 0.95f))
        )
    }

    Scaffold(
        topBar = {
            if (activeChatTarget == null && !showProfileScreen && selectedBottomNavIndex == 0 && selectedCategory == null && selectedListingDetail == null) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LOVELY HUB",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color.White,
                                letterSpacing = 1.2.sp
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showProfileScreen = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (userPhotoUrl != null) {
                                AsyncImage(
                                    model = userPhotoUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Profile",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF673AB7))
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E1E2D),
                tonalElevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                val navItems = listOf(
                    "Home" to Icons.Default.Home,
                    "Add" to Icons.Default.AddBox,
                    "Chats" to Icons.AutoMirrored.Filled.Chat,
                    "About" to Icons.Default.Info
                )

                navItems.forEachIndexed { index, (label, icon) ->
                    val isSelected = activeChatTarget == null && !showProfileScreen && selectedBottomNavIndex == index && selectedCategory == null && selectedListingDetail == null
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            activeChatTarget = null
                            showProfileScreen = false
                            selectedBottomNavIndex = index
                            selectedCategory = null
                            selectedListingDetail = null
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFFD54F),
                            selectedTextColor = Color(0xFFFFD54F),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFFFFD54F).copy(alpha = 0.22f)
                        )
                    )
                }
            }
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
                    showProfileScreen -> {
                        ProfileScreen(
                            viewModel = viewModel,
                            onSignOut = onSignOut
                        )
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
                        AboutCompanyScreen()
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp)
                                    .clickable { showProfileScreen = true },
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (userPhotoUrl != null) {
                                        AsyncImage(
                                            model = userPhotoUrl,
                                            contentDescription = "User Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .border(2.5.dp, Color(0xFF673AB7), CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF673AB7).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF673AB7),
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = "Welcome 👋",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF673AB7)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = userName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1E2D)
                                        )
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(categories) { category ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            selectedCategory = category.title
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(86.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(category.bgColor)
                                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = category.title,
                                                tint = category.iconColor,
                                                modifier = Modifier.size(38.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = category.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
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

@Composable
fun AboutCompanyScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .border(3.5.dp, Color.White, CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "K",
                fontSize = 58.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Kaisar Ahmad Lone",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E1E2D)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Android Developer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF673AB7)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Developer of LovelyHub app",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E1E2D)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "If you want any custom mobile application, website, or enterprise software solution developed, feel free to contact me.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

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
            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1E1E2D), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "lonekaisar15@gmail.com",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2D)
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
            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E1E2D), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "9086455406",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2D)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https:" + "/" + "/github.com/KaisarAhLone"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF1E1E2D), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "GitHub Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2D)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https:" + "/" + "/www.linkedin.com/in/kaisar-ah-lone-/"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF1E1E2D), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "LinkedIn Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2D)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.6f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "View Portfolio",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF673AB7),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https:" + "/" + "/github.com/KaisarAhLone"))
                try { context.startActivity(intent) } catch (_: Exception) {}
            }
        )
    }
}
