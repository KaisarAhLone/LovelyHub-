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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.lovelyhub.ui.components.MapLocationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listing: Listing,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFFFFB300)
        )
    )

    fun cleanLocationName(rawLocation: String): String {
        if (rawLocation.isBlank()) return "LPU Campus"
        val idx = rawLocation.indexOf("(")
        return if (idx != -1) rawLocation.substring(0, idx).trim() else rawLocation.trim()
    }

    val displayLocation = cleanLocationName(listing.location)

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF1E1E2D)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = listing.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1E2D)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (listing.imageUrl.isNotBlank()) {
                                    ListingImage(
                                        imageUrl = listing.imageUrl,
                                        contentDescription = listing.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(86.dp)
                                            .clip(CircleShape)
                                            .border(2.5.dp, Color(0xFF673AB7), CircleShape)
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
                                            .size(86.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF673AB7).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF673AB7),
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = listing.title,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1E2D)
                                    )

                                    if (displayLocation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = displayLocation,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1E1E2D),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = Color(0xFF673AB7),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (listing.is24Hours) "Open 24 Hours" else "${listing.openTime} - ${listing.closeTime}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF673AB7),
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E7D32))
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = listing.rating.ifBlank { "4.3" },
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (listing.phone.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.phone}"))
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        MapLocationHelper.openMapDirections(context, listing.location)
                                    },
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Get Directions", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (listing.phone.isNotBlank()) {
                                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${listing.phone}"))
                                            intent.putExtra("sms_body", "Hi, I want to order from '${listing.title}' on Lovely Hub.")
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier
                                        .width(46.dp)
                                        .height(46.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF673AB7)),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = Color(0xFF673AB7),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "FOOD MENU & DISHES",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1E2D)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF673AB7))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${listing.menuItems.size} Items",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (listing.menuItems.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No dishes added yet by ${listing.title}.",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        listing.menuItems.forEach { itemEntry ->
                            val parts = itemEntry.split("|")
                            val titleAndPrice = parts[0]
                            val dishPhoto = if (parts.size > 1) parts[1] else ""

                            val tpParts = titleAndPrice.split(" - ")
                            val dishName = tpParts[0]
                            val dishPrice = if (tpParts.size > 1) tpParts[1] else ""

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dishName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E1E2D)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = dishPrice,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF673AB7)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Freshly prepared at ${listing.title}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Box(contentAlignment = Alignment.BottomCenter) {
                                        if (dishPhoto.isNotBlank()) {
                                            ListingImage(
                                                imageUrl = dishPhoto,
                                                contentDescription = dishName,
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
                                                    .background(Color(0xFFF0E5FC)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Restaurant,
                                                    contentDescription = null,
                                                    tint = Color(0xFF673AB7),
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF673AB7))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                Text("ADD", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
}
