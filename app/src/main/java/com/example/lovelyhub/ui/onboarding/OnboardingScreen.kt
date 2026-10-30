package com.example.lovelyhub.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class PerfectlyBalancedCard(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val cardBg: Color,
    val offsetX: Int,
    val offsetY: Int,
    val rotationAngle: Float = 0f
)

data class RefinedSlideModel(
    val brandPrefix: String,
    val brandSuffix: String,
    val topSubtitle1: String,
    val topSubtitle2: String,
    val cards: List<PerfectlyBalancedCard>,
    val bottomHeading1: String,
    val bottomHeading2: String
)

val refinedOnboardingSlides = listOf(
    // Slide 1: Welcome (Exact Recreation of Reference Image)
    RefinedSlideModel(
        brandPrefix = "Lovely",
        brandSuffix = "Hub",
        topSubtitle1 = "Your Campus Marketplace",
        topSubtitle2 = "Buy • Sell • Rent • Find Jobs • Services\nFood • Explore More",
        bottomHeading1 = "Everything you need,",
        bottomHeading2 = "Near your Campus!",
        cards = listOf(
            PerfectlyBalancedCard("Marketplace", Icons.Default.ShoppingCart, Color(0xFFFF2A85), Color(0xFFFFF0F6), -120, -5, -6f),
            PerfectlyBalancedCard("Jobs", Icons.Default.Work, Color(0xFF10B981), Color(0xFFF0FDF4), -58, 62, -2f),
            PerfectlyBalancedCard("Rooms & PGs", Icons.Default.Home, Color(0xFF2563EB), Color(0xFFEFF6FF), 0, -32, 0f),
            PerfectlyBalancedCard("Services", Icons.Default.Build, Color(0xFF8B5CF6), Color(0xFFF5F3FF), 58, 62, 2f),
            PerfectlyBalancedCard("Restaurants", Icons.Default.Restaurant, Color(0xFFF97316), Color(0xFFFFF7ED), 120, -5, 6f)
        )
    ),

    // Slide 2: Marketplace (Tech, Books, Gadgets & Cycles)
    RefinedSlideModel(
        brandPrefix = "Student",
        brandSuffix = "Marketplace",
        topSubtitle1 = "Buy & Sell Peer-to-Peer",
        topSubtitle2 = "Laptops • Mobiles • Books • Cycles • Tech\n0% Commission • Instant Cash",
        bottomHeading1 = "Buy & Sell Safely,",
        bottomHeading2 = "Within Campus!",
        cards = listOf(
            PerfectlyBalancedCard("Laptops", Icons.Default.Laptop, Color(0xFFFF2A85), Color(0xFFFFF0F6), -120, -5, -6f),
            PerfectlyBalancedCard("Mobiles", Icons.Default.Smartphone, Color(0xFF0284C7), Color(0xFFF0F9FF), -58, 62, -2f),
            PerfectlyBalancedCard("Textbooks", Icons.AutoMirrored.Filled.MenuBook, Color(0xFF8B5CF6), Color(0xFFF5F3FF), 0, -32, 0f),
            PerfectlyBalancedCard("Cycles", Icons.Default.PedalBike, Color(0xFF10B981), Color(0xFFF0FDF4), 58, 62, 2f),
            PerfectlyBalancedCard("Verified IDs", Icons.Default.CheckCircle, Color(0xFFF59E0B), Color(0xFFFFFBEB), 120, -5, 6f)
        )
    ),

    // Slide 3: Campus Food & Dining
    RefinedSlideModel(
        brandPrefix = "Campus",
        brandSuffix = "Dining",
        topSubtitle1 = "Delicious Food & Cravings",
        topSubtitle2 = "Cafes • Stalls • Night Meals • Shakes\nDirect WhatsApp • GPS Gate Drop",
        bottomHeading1 = "Delicious Food Drop,",
        bottomHeading2 = "Direct to your Gate!",
        cards = listOf(
            PerfectlyBalancedCard("Hot Meals", Icons.Default.Restaurant, Color(0xFFF97316), Color(0xFFFFF7ED), -120, -5, -6f),
            PerfectlyBalancedCard("Campus Cafes", Icons.Default.Coffee, Color(0xFF8B5CF6), Color(0xFFF5F3FF), -58, 62, -2f),
            PerfectlyBalancedCard("GPS Gate Pin", Icons.Default.LocationOn, Color(0xFFEF4444), Color(0xFFFEF2F2), 0, -32, 0f),
            PerfectlyBalancedCard("Midnight Crave", Icons.Default.FlashOn, Color(0xFFF59E0B), Color(0xFFFEFCE8), 58, 62, 2f),
            PerfectlyBalancedCard("₹0 Extra Fee", Icons.Default.CheckCircle, Color(0xFF10B981), Color(0xFFF0FDF4), 120, -5, 6f)
        )
    ),

    // Slide 4: Rooms, PGs & Vehicles
    RefinedSlideModel(
        brandPrefix = "Rooms",
        brandSuffix = "& Rentals",
        topSubtitle1 = "Flats, PGs & Easy Mobility",
        topSubtitle2 = "Wi-Fi • AC • Food Included • Bikes & Cars\nZero Brokerage • Direct Owner Call",
        bottomHeading1 = "Hostels & Rides,",
        bottomHeading2 = "Zero Brokerage!",
        cards = listOf(
            PerfectlyBalancedCard("Flats & PGs", Icons.Default.Home, Color(0xFF0284C7), Color(0xFFF0F9FF), -120, -5, -6f),
            PerfectlyBalancedCard("Hourly Bikes", Icons.Default.PedalBike, Color(0xFF10B981), Color(0xFFF0FDF4), -58, 62, -2f),
            PerfectlyBalancedCard("Direct Call", Icons.Default.CheckCircle, Color(0xFFFF2A85), Color(0xFFFFF0F6), 0, -32, 0f),
            PerfectlyBalancedCard("Car Rentals", Icons.Default.DirectionsCar, Color(0xFF7C3AED), Color(0xFFF5F3FF), 58, 62, 2f),
            PerfectlyBalancedCard("Verified Pics", Icons.Default.Shield, Color(0xFFF59E0B), Color(0xFFFFFBEB), 120, -5, 6f)
        )
    ),

    // Slide 5: Instant Chat & Services (Final slide)
    RefinedSlideModel(
        brandPrefix = "Instant",
        brandSuffix = "Connect",
        topSubtitle1 = "0ms Chat & Doorstep Services",
        topSubtitle2 = "Offline Cached Chat • Laundry • Repairs\n100% Student-First Community",
        bottomHeading1 = "Your Campus Journey,",
        bottomHeading2 = "Starts Right Here!",
        cards = listOf(
            PerfectlyBalancedCard("Instant Chat", Icons.AutoMirrored.Filled.Chat, Color(0xFFFF2A85), Color(0xFFFFF0F6), -120, -5, -6f),
            PerfectlyBalancedCard("Laundry", Icons.Default.LocalLaundryService, Color(0xFF0284C7), Color(0xFFF0F9FF), -58, 62, -2f),
            PerfectlyBalancedCard("Safe Campus", Icons.Default.Shield, Color(0xFF10B981), Color(0xFFF0FDF4), 0, -32, 0f),
            PerfectlyBalancedCard("Repairs", Icons.Default.Build, Color(0xFF8B5CF6), Color(0xFFF5F3FF), 58, 62, 2f),
            PerfectlyBalancedCard("Let's Go!", Icons.Default.RocketLaunch, Color(0xFFF59E0B), Color(0xFFFFFBEB), 120, -5, 6f)
        )
    )
)

@Composable
fun OnboardingScreen(
    slides: List<RefinedSlideModel> = refinedOnboardingSlides,
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == slides.size - 1

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val hotPink = Color(0xFFFF2A85)
    val darkNavy = Color(0xFF1E1B4B)
    val softMutedText = Color(0xFF64748B)

    // Base background matching reference image: Soft pastel pink & white
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF4F8),
                        Color(0xFFFFF8FA),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        // Soft organic pink curve at top-right
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = 65.dp, y = (-55).dp)
                .clip(CircleShape)
                .background(Color(0xFFFFDDEB).copy(alpha = 0.65f))
        )

        // Cute scattered pastel bubbles
        Box(
            modifier = Modifier
                .size(16.dp)
                .offset(x = 35.dp, y = (statusBarPadding + 145.dp))
                .clip(CircleShape)
                .background(Color(0xFFFBCFE8))
        )
        Box(
            modifier = Modifier
                .size(11.dp)
                .offset(x = 28.dp, y = (statusBarPadding + 225.dp))
                .clip(CircleShape)
                .background(Color(0xFF99F6E4).copy(alpha = 0.7f))
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = (statusBarPadding + 155.dp))
                .clip(CircleShape)
                .background(Color(0xFFBAE6FD).copy(alpha = 0.8f))
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-38).dp, y = (statusBarPadding + 245.dp))
                .clip(CircleShape)
                .background(Color(0xFFFEF08A).copy(alpha = 0.75f))
        )

        // Bottom gentle wave backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 85.dp, topEnd = 85.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFEEF5).copy(alpha = 0.55f),
                            Color(0xFFFFF7FA),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
        )

        // Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val slide = slides[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = statusBarPadding + 16.dp,
                        bottom = navBarPadding + 90.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Brand Title: "LovelyHub" + Pink Burst
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = slide.brandPrefix,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = hotPink,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = slide.brandSuffix,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = darkNavy,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // 3 Pink Burst Rays
                    Canvas(
                        modifier = Modifier
                            .size(22.dp)
                            .padding(start = 2.dp, top = 2.dp)
                    ) {
                        val stroke = 3.2f
                        val color = Color(0xFFFF2A85)
                        drawLine(color, Offset(5f, 16f), Offset(1f, 10f), strokeWidth = stroke)
                        drawLine(color, Offset(11f, 11f), Offset(17f, 3f), strokeWidth = stroke)
                        drawLine(color, Offset(15f, 16f), Offset(23f, 16f), strokeWidth = stroke)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle 1
                Text(
                    text = slide.topSubtitle1,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle 2
                Text(
                    text = slide.topSubtitle2,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = softMutedText,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Symmetrical Center Arc of 5 Cards (Balanced, Clean, Never Crooked)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Dashed Semicircle Arc behind cards
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFE2E8F0),
                            startAngle = 195f,
                            sweepAngle = 150f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }

                    // Symmetrically positioned squircle cards
                    slide.cards.forEach { card ->
                        Box(
                            modifier = Modifier
                                .offset(x = card.offsetX.dp, y = card.offsetY.dp)
                                .graphicsLayer(rotationZ = card.rotationAngle)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                shadowElevation = 6.dp,
                                modifier = Modifier
                                    .size(width = 78.dp, height = 78.dp)
                                    .border(1.2.dp, card.cardBg, RoundedCornerShape(20.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.White, card.cardBg.copy(alpha = 0.6f))
                                            )
                                        )
                                        .padding(vertical = 8.dp, horizontal = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = card.icon,
                                        contentDescription = card.title,
                                        tint = card.iconColor,
                                        modifier = Modifier.size(30.dp)
                                    )

                                    Text(
                                        text = card.title,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = darkNavy,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Bold 2-Line Heading (Centered, High Contrast)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = slide.bottomHeading1,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = darkNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = slide.bottomHeading2,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        color = hotPink,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Fixed Bottom Navigation Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = navBarPadding + 22.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Button (Left)
                if (!isLastPage) {
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = softMutedText,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onFinishOnboarding() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                // Center: Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(slides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) hotPink else Color(0xFFE2E8F0),
                            label = "DotColor"
                        )
                        val dotSize by animateDpAsState(
                            targetValue = if (isSelected) 10.dp else 8.dp,
                            label = "DotSize"
                        )

                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                // Action Button (Right: Pink Circle Arrow OR Get Started)
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "OnboardingActionButton"
                ) { last ->
                    if (!last) {
                        Surface(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = CircleShape,
                            color = hotPink,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(58.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF3E96), Color(0xFFFF1475))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onFinishOnboarding,
                            colors = ButtonDefaults.buttonColors(containerColor = hotPink),
                            shape = RoundedCornerShape(30.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Get Started",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
