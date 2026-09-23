package com.example.lovelyhub.ui.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lovelyhub.ui.auth.AuthViewModel

data class HomeCategoryItem(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onSignOut: () -> Unit
) {
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser
    var searchQuery by remember { mutableStateOf("") }
    var selectedBottomNavIndex by remember { mutableStateOf(0) }

    val categories = remember {
        listOf(
            HomeCategoryItem("Food", Icons.Default.Restaurant, Color(0xFFD89B00), Color(0xFFFFF8E1)),
            HomeCategoryItem("Rooms", Icons.Default.HomeWork, Color(0xFF1E88E5), Color(0xFFE3F2FD)),
            HomeCategoryItem("Rentals", Icons.Default.DirectionsCar, Color(0xFF1976D2), Color(0xFFE8F0FE)),
            HomeCategoryItem("Jobs", Icons.Default.Work, Color(0xFF5E35B1), Color(0xFFEDE7F6)),
            HomeCategoryItem("Marketplace", Icons.Default.Storefront, Color(0xFFD81B60), Color(0xFFFCE4EC)),
            HomeCategoryItem("Services", Icons.Default.Build, Color(0xFF43A047), Color(0xFFE8F5E9)),
            HomeCategoryItem("Open Now", Icons.Default.Schedule, Color(0xFF2E7D32), Color(0xFFE8F5E9)),
            HomeCategoryItem("Favorites", Icons.Default.Favorite, Color(0xFFE53935), Color(0xFFFFEBEE)),
            HomeCategoryItem("More", Icons.Default.MoreHoriz, Color(0xFF616161), Color(0xFFF5F5F5))
        )
    }

    Scaffold(
        topBar = {
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
                            color = Color(0xFF1E1E2D),
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        onSignOut()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = Color(0xFF7926E1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                val navItems = listOf(
                    "Home" to Icons.Default.Home,
                    "Search" to Icons.Default.Search,
                    "Add" to Icons.Default.AddBox,
                    "Chats" to Icons.AutoMirrored.Filled.Chat,
                    "Profile" to Icons.Default.Person
                )

                navItems.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedBottomNavIndex == index,
                        onClick = { selectedBottomNavIndex = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1A237E),
                            selectedTextColor = Color(0xFF1A237E),
                            indicatorColor = Color(0xFF1A237E).copy(alpha = 0.12f)
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
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search anything...", fontSize = 15.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFFAFAFA),
                        focusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1E1E2D),
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(categories) { category ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(category.bgColor)
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.title,
                                    tint = category.iconColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = category.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF212121),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
