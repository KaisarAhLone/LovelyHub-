package com.example.lovelyhub.ui.listings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
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

    LaunchedEffect(category) {
        viewModel.fetchListings(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E1E2D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E1E2D)
                        )
                    }
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
                when (val state = listingState) {
                    is ListingState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF7926E1))
                        }
                    }
                    is ListingState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = state.message, color = Color.Red, fontSize = 15.sp)
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
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = onAddListingClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7926E1)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Add First Listing in $category", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (listing.imageUrl.isNotBlank()) {
                ListingImage(
                    imageUrl = listing.imageUrl,
                    contentDescription = listing.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
                )
            } else {
                val icon = when (listing.category) {
                    "Food" -> Icons.Default.Restaurant
                    "Rooms" -> Icons.Default.HomeWork
                    "Rentals" -> Icons.Default.DirectionsCar
                    "Jobs" -> Icons.Default.Work
                    "Marketplace" -> Icons.Default.ShoppingBag
                    else -> Icons.Default.Storefront
                }
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF0E5FC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF7926E1),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listing.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E2D)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = listing.price,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7926E1)
                )

                if (listing.phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Phone: ${listing.phone}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (listing.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (listing.isFavorite) Color(0xFFE53935) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (isOwner) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7926E1).copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF7926E1),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
