package com.example.lovelyhub.ui.chats

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.components.MapLocationHelper
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    receiverUid: String,
    receiverName: String,
    receiverPhotoUrl: String = "",
    viewModel: ChatViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val colorfulGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF673AB7),
            Color(0xFF00BCD4),
            Color(0xFF00E676),
            Color(0xFF1DE9B6)
        )
    )

    LaunchedEffect(receiverUid) {
        viewModel.listenToMessages(receiverUid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (receiverPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = receiverPhotoUrl,
                                contentDescription = receiverName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Text(
                            text = receiverName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF673AB7))
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorfulGradient)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            val isMe = msg.senderUid == currentUid
                            val formattedTime = try {
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                            } catch (_: Exception) {
                                ""
                            }

                            if (msg.messageText.contains("NEW CAMPUS ORDER PLACED!")) {
                                OrderCardBubble(
                                    context = context,
                                    rawText = msg.messageText,
                                    formattedTime = formattedTime
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isMe) 16.dp else 2.dp,
                                                    bottomEnd = if (isMe) 2.dp else 16.dp
                                                )
                                            )
                                            .background(if (isMe) Color(0xFF673AB7) else Color.White.copy(alpha = 0.95f))
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.messageText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isMe) Color.White else Color(0xFF1E1E2D)
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = formattedTime,
                                                fontSize = 10.sp,
                                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                                modifier = Modifier.align(Alignment.End)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E2D))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                focusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF673AB7),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(receiverUid, inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD54F))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color(0xFF1E1E2D),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardBubble(
    context: Context,
    rawText: String,
    formattedTime: String
) {
    val lines = rawText.split("\n")
    val itemLines = lines.filter { it.trim().startsWith("•") }

    val totalLine = lines.firstOrNull { it.contains("Grand Total:") } ?: ""
    val nameLine = lines.firstOrNull { it.contains("Customer:") }?.replace("👤 Customer:", "")?.trim() ?: ""
    val phoneLine = lines.firstOrNull { it.contains("Phone:") }?.replace("📞 Phone:", "")?.trim() ?: ""
    val pgLine = lines.firstOrNull { it.contains("PG/Hostel:") }?.replace("🏠 PG/Hostel:", "")?.trim() ?: ""
    val locationLine = lines.firstOrNull { it.contains("Location:") }?.replace("📍 Location:", "")?.trim() ?: ""

    fun sendOrderToWhatsApp() {
        val cleanNum = phoneLine.replace(Regex("[^0-9]"), "")
        val fullNum = if (!cleanNum.startsWith("91") && cleanNum.length == 10) "91$cleanNum" else cleanNum

        val cleanMsgForWA = rawText.replace(Regex("\\[IMAGE:.*?\\]"), "")
        val encodedMsg = Uri.encode(cleanMsgForWA)

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2E7D32))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🍔 NEW CAMPUS ORDER",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(
                    text = formattedTime,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ordered Items:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF673AB7)
            )

            Spacer(modifier = Modifier.height(6.dp))

            itemLines.forEach { line ->
                val imageIdx = line.indexOf("[IMAGE:")
                val photoUrl = if (imageIdx != -1) {
                    line.substring(imageIdx + 7).replace("]", "").trim()
                } else ""

                val cleanText = if (imageIdx != -1) {
                    line.substring(0, imageIdx).trim()
                } else {
                    line.trim()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (photoUrl.isNotBlank()) {
                        ListingImage(
                            imageUrl = photoUrl,
                            contentDescription = cleanText,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF0E5FC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color(0xFF673AB7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = cleanText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E2D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            if (totalLine.isNotBlank()) {
                Text(
                    text = totalLine,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "Delivery Fee: ₹0 (FREE) • Platform Fee: ₹0 (FREE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD84315)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF6F7FB))
                    .padding(10.dp)
            ) {
                Text("Customer & Delivery Info:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF673AB7))
                Spacer(modifier = Modifier.height(2.dp))
                if (nameLine.isNotBlank()) Text("👤 Name: $nameLine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E2D))
                if (phoneLine.isNotBlank()) Text("📞 Phone: $phoneLine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E2D))
                if (pgLine.isNotBlank()) Text("🏠 PG/Hostel: $pgLine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E2D))
                if (locationLine.isNotBlank()) Text("📍 Address: $locationLine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1E2D))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { sendOrderToWhatsApp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Smartphone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Forward Order on WhatsApp 📲", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (phoneLine.isNotBlank()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneLine"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (locationLine.isNotBlank()) {
                    Button(
                        onClick = {
                            MapLocationHelper.openMapDirections(context, locationLine)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Location", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
