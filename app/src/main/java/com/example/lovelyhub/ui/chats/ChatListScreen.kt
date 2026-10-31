package com.example.lovelyhub.ui.chats

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.data.local.LocalUserCache
import com.example.lovelyhub.data.model.ChatConversation
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.theme.LovelyDarkPinkBg
import com.example.lovelyhub.ui.theme.LovelyDarkPinkGradient
import com.example.lovelyhub.ui.theme.LovelyDarkPinkSoft
import com.example.lovelyhub.ui.theme.LovelyGreenOnline
import com.example.lovelyhub.ui.theme.LovelyHeaderDarkPinkGradient
import com.example.lovelyhub.ui.theme.LovelyPurplePrimary
import com.example.lovelyhub.ui.theme.LovelyTextDark
import com.example.lovelyhub.ui.theme.LovelyTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
    viewModel: ChatViewModel = viewModel(),
    onConversationClick: (ChatConversation) -> Unit
) {
    val context = LocalContext.current
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoadingConversations.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.listenToConversations(context)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LovelyDarkPinkBg
    ) {
        val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LovelyDarkPinkGradient)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                        .background(LovelyHeaderDarkPinkGradient)
                        .padding(start = 20.dp, end = 20.dp, top = statusBarPadding + 14.dp, bottom = 20.dp)
                ) {
                    Column {
                        Text(
                            text = "Messages & Chats",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Instant chats cached locally on your device",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    isLoading && conversations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = LovelyDarkPinkSoft)
                        }
                    }
                    conversations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages yet!\nClick Message on any Restaurant or Listing to start chatting.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = LovelyDarkPinkSoft,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(conversations) { conv ->
                                val formattedTime = try {
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conv.timestamp))
                                } catch (_: Exception) {
                                    ""
                                }

                                val isUnread = conv.unreadCount > 0
                                val localPhoto = remember(conv.otherUid) {
                                    if (conv.otherUid.isNotBlank()) LocalUserCache(context).getProfilePhoto(conv.otherUid) else null
                                }
                                val photoToDisplay = conv.otherPhotoUrl.ifBlank { localPhoto ?: "" }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onConversationClick(conv) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isUnread) Color(0xFFFDF2F8) else Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 4.dp else 2.dp),
                                    border = if (isUnread) BorderStroke(1.2.dp, Color(0xFF25D366)) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.size(52.dp)) {
                                            if (photoToDisplay.isNotBlank()) {
                                                ListingImage(
                                                    imageUrl = photoToDisplay,
                                                    contentDescription = conv.otherName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .border(2.dp, if (isUnread) Color(0xFF25D366) else LovelyPurplePrimary, CircleShape)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(LovelyPurplePrimary.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = LovelyPurplePrimary,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                }
                                            }

                                            if (isUnread) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(15.dp)
                                                        .align(Alignment.TopEnd)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF25D366))
                                                        .border(2.dp, Color.White, CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = conv.otherName,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = LovelyTextDark
                                                )

                                                Text(
                                                    text = formattedTime,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Normal,
                                                    color = if (isUnread) Color(0xFF25D366) else LovelyTextMuted
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = conv.lastMessage,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isUnread) Color(0xFF1E1E2D) else LovelyTextMuted,
                                                    maxLines = 1,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                if (isUnread) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color(0xFF25D366))
                                                            .padding(horizontal = 8.dp, vertical = 3.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "NEW",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold
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
            }
        }
    }
}
