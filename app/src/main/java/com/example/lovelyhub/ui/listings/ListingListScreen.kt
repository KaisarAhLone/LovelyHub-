package com.example.lovelyhub.ui.listings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.components.ListingImage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFFFFB300)
        )
    )

    LaunchedEffect(category) {
        viewModel.fetchListings(category)
    }

    Scaffold(
        containerColor = Color(0xFF673AB7)
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
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF673AB7))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = category,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (val state = listingState) {
                            is ListingState.Loading -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                            is ListingState.Error -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = state.message, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            is ListingState.Success -> {
                                val items = state.items
                                if (items.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "No listings in $category yet!",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(
                                                onClick = onAddListingClick,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Add First Listing in $category", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                                            }
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(items) { listing ->
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
    onFavoriteToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isOpen = listing.status.equals("Open", ignoreCase = true)
    val cardBg = if (isOpen) Color.White.copy(alpha = 0.95f) else Color(0xFFEEEEEE).copy(alpha = 0.95f)

    fun cleanLocationName(rawLocation: String): String {
        if (rawLocation.isBlank()) return "LPU Campus"
        val idx = rawLocation.indexOf("(")
        return if (idx != -1) rawLocation.substring(0, idx).trim() else rawLocation.trim()
    }

    val displayLocation = cleanLocationName(listing.location)

    val priceDisplay = if (listing.price.startsWith("Open", ignoreCase = true)) {
        "Starting ₹100"
    } else if (listing.price.contains("Starting", ignoreCase = true)) {
        listing.price
    } else {
        "Starting ${listing.price}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOpen) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                if (listing.imageUrl.isNotBlank()) {
                    ListingImage(
                        imageUrl = listing.imageUrl,
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
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF673AB7),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onFavoriteToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (listing.isFavorite) Color(0xFFFF1744) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color(0xFF1565C0).copy(alpha = 0.88f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (listing.is24Hours) "⚡ Open 24 Hours • Free Delivery" else "⏰ Open: ${listing.openTime} - ${listing.closeTime}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1E2D),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = listing.rating.ifBlank { "4.3" },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (displayLocation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = displayLocation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = priceDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD84315)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOpen) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isOpen) "OPEN" else "CLOSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOpen) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isOwner) {
                            IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF673AB7))
                                .clickable { onCallClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
