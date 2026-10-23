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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    var activeChatTarget by remember { mutableStateOf<ActiveChatTarget?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser

    val userPhotoUrl = userProfile?.photoUrl?.ifBlank { null } ?: currentUser?.photoUrl?.toString()
    val userName = userProfile?.name?.ifBlank { null } ?: currentUser?.displayName ?: "Ky Sr"

    BackHandler(enabled = activeChatTarget != null || showAboutScreen || selectedListingDetail != null || selectedCategory != null || selectedBottomNavIndex != 0) {
        when {
            activeChatTarget != null -> {
                activeChatTarget = null
            }
            showAboutScreen -> {
                showAboutScreen = false
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

    val topHeaderGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF3F51B5),
            Color(0xFF5C6BC0),
            Color(0xFFE8EAF6)
        )
    )

    Scaffold(
        topBar = {
            if (activeChatTarget == null && !showAboutScreen && selectedBottomNavIndex == 0 && selectedCategory == null && selectedListingDetail == null) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LovelyHub",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { showAboutScreen = true }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About Developer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "About",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
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
                    .background(Color(0xFFF3F4F9))
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
                        AboutCompanyScreen()
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(topHeaderGradient)
                                    .padding(horizontal = 18.dp)
                                    .padding(top = 10.dp, bottom = 20.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBottomNavIndex = 3 },
                                    shape = RoundedCornerShape(22.dp),
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
                                            Box(contentAlignment = Alignment.BottomEnd) {
                                                if (userPhotoUrl != null) {
                                                    AsyncImage(
                                                        model = userPhotoUrl,
                                                        contentDescription = "User Photo",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(56.dp)
                                                            .clip(CircleShape)
                                                            .border(2.dp, Color(0xFF3F51B5), CircleShape)
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(56.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF3F51B5).copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(30.dp))
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF00E676))
                                                        .border(1.5.dp, Color.White, CircleShape)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Text("Welcome 👋", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3F51B5))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1E2D))
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE8EAF6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFF3F51B5), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search here...", color = Color.Gray, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE8EAF6)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Tune, contentDescription = "Filter", tint = Color(0xFF3F51B5), modifier = Modifier.size(18.dp))
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(26.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    HomeCategoryBox(
                                        title = "Restaurants",
                                        icon = Icons.Default.Restaurant,
                                        iconTileBg = Color(0xFFFF9800),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Restaurants" }
                                    )

                                    HomeCategoryBox(
                                        title = "Rooms",
                                        icon = Icons.Default.HomeWork,
                                        iconTileBg = Color(0xFF03A9F4),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Rooms" }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    HomeCategoryBox(
                                        title = "Rentals",
                                        icon = Icons.Default.DirectionsCar,
                                        iconTileBg = Color(0xFF7E57C2),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Rentals" }
                                    )

                                    HomeCategoryBox(
                                        title = "Jobs",
                                        icon = Icons.Default.Work,
                                        iconTileBg = Color(0xFF4CAF50),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xE8E8F5E9), Color(0xFFC8E6C9))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Jobs" }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    HomeCategoryBox(
                                        title = "Marketplace",
                                        icon = Icons.Default.Storefront,
                                        iconTileBg = Color(0xFFE91E63),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Marketplace" }
                                    )

                                    HomeCategoryBox(
                                        title = "Services",
                                        icon = Icons.Default.Build,
                                        iconTileBg = Color(0xFFFFB300),
                                        bgGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3))),
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedCategory = "Services" }
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCategory = "Favorites" },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFFFFEBEE), Color(0xFFFFCDD2))
                                                )
                                            )
                                            .padding(horizontal = 18.dp, vertical = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFFE53935)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Favorite,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Text(
                                                    text = "Favorites",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF1E1E2D)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                    contentDescription = null,
                                                    tint = Color(0xFFE53935),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCategoryBox(
    title: String,
    icon: ImageVector,
    iconTileBg: Color,
    bgGradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconTileBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1E2D)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = iconTileBg,
                            modifier = Modifier.size(12.dp)
                        )
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
            color = Color(0xFF3F51B5)
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
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
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
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "View Portfolio",
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3F51B5),
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https:" + "/" + "/github.com/KaisarAhLone"))
                try { context.startActivity(intent) } catch (_: Exception) {}
            }
        )
    }
}
