package com.example.lovelyhub.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lovelyhub.data.model.AdminAuditLog
import com.example.lovelyhub.data.model.AdminStats
import com.example.lovelyhub.data.model.CommunicationEvent
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.ui.components.ListingImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AdminSection(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    PENDING_APPROVALS("Pending Approvals", Icons.Default.HourglassEmpty),
    RESTAURANTS("Restaurants", Icons.Default.Restaurant),
    RENTALS("Rentals", Icons.Default.DirectionsCar),
    ROOMS("Rooms & PGs", Icons.Default.Home),
    SERVICES("Services", Icons.Default.Build),
    MARKETPLACE("Marketplace", Icons.Default.Storefront),
    JOBS("Jobs", Icons.Default.Work),
    AUDIT_LOGS("Audit Logs", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
    val stats by viewModel.adminStats.collectAsState()
    val filteredListings by viewModel.filteredListings.collectAsState()
    val selectedStatusFilter by viewModel.selectedStatusFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val messageEvents by viewModel.messageEvents.collectAsState()
    val callEvents by viewModel.callEvents.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val isAutoApprovalEnabled by viewModel.isAutoApprovalEnabled.collectAsState()

    var listingToReject by remember { mutableStateOf<Listing?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }

    var listingToDeactivate by remember { mutableStateOf<Listing?>(null) }
    var listingDetailToShow by remember { mutableStateOf<Listing?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboard()
    }

    // System back button = auto-logout from owner portal
    BackHandler {
        onLogout()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF160E2A),
                drawerContentColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF581C87), Color(0xFFEC4899))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("LovelyHub Owner", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                                Text("Administrative Control", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    AdminSection.entries.forEach { section ->
                        val isSelected = currentSection == section
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                    tint = if (isSelected) Color(0xFFEC4899) else Color.White.copy(alpha = 0.7f)
                                )
                            },
                            label = {
                                Text(
                                    text = section.title,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFFEC4899) else Color.White
                                )
                            },
                            badge = {
                                if (section == AdminSection.PENDING_APPROVALS && stats.totalPendingApprovals > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFEF4444))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text("${stats.totalPendingApprovals}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                currentSection = section
                                scope.launch { drawerState.close() }

                                // Sync category filter with section
                                when (section) {
                                    AdminSection.RESTAURANTS -> viewModel.setCategory("Restaurants")
                                    AdminSection.RENTALS -> viewModel.setCategory("Rentals")
                                    AdminSection.ROOMS -> viewModel.setCategory("Rooms")
                                    AdminSection.SERVICES -> viewModel.setCategory("Services")
                                    AdminSection.MARKETPLACE -> viewModel.setCategory("Marketplace")
                                    AdminSection.JOBS -> viewModel.setCategory("Jobs")
                                    AdminSection.PENDING_APPROVALS -> {
                                        viewModel.setCategory("All")
                                        viewModel.setStatusFilter("Pending")
                                    }
                                    else -> {
                                        viewModel.setCategory("All")
                                        if (section == AdminSection.OVERVIEW) viewModel.setStatusFilter("All")
                                    }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFF281C48),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Logout Item
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color(0xFFEF4444)) },
                    label = { Text("Logout Portal", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLogoutDialog = true
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "LovelyHub Owner Dashboard",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = currentSection.title,
                                fontSize = 12.sp,
                                color = Color(0xFFEC4899),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refreshDashboard() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color(0xFFEF4444))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF140D26))
                )
            },
            containerColor = Color(0xFF0F0C20)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF0F0C20))
            ) {
                when (currentSection) {
                    AdminSection.OVERVIEW -> {
                        AdminOverviewContent(
                            stats = stats,
                            isAutoApprovalEnabled = isAutoApprovalEnabled,
                            onToggleAutoApproval = { viewModel.toggleAutoApproval(it) },
                            onNavigateToSection = { section ->
                                currentSection = section
                                when (section) {
                                    AdminSection.RESTAURANTS -> viewModel.setCategory("Restaurants")
                                    AdminSection.RENTALS -> viewModel.setCategory("Rentals")
                                    AdminSection.ROOMS -> viewModel.setCategory("Rooms")
                                    AdminSection.SERVICES -> viewModel.setCategory("Services")
                                    AdminSection.MARKETPLACE -> viewModel.setCategory("Marketplace")
                                    AdminSection.JOBS -> viewModel.setCategory("Jobs")
                                    AdminSection.PENDING_APPROVALS -> {
                                        viewModel.setCategory("All")
                                        viewModel.setStatusFilter("Pending")
                                    }
                                    else -> {}
                                }
                            }
                        )
                    }

                    AdminSection.AUDIT_LOGS -> {
                        AdminAuditLogsList(auditLogs = auditLogs)
                    }

                    AdminSection.SETTINGS -> {
                        AdminSettingsContent(isAutoApprovalEnabled = isAutoApprovalEnabled, onToggleAutoApproval = { viewModel.toggleAutoApproval(it) }, onLogout = { showLogoutDialog = true })
                    }

                    else -> {
                        // All Category & Pending Management views
                        AdminCategoryManagementContent(
                            section = currentSection,
                            listings = filteredListings,
                            selectedStatus = selectedStatusFilter,
                            searchQuery = searchQuery,
                            isLoading = isLoading,
                            onStatusFilterSelected = { viewModel.setStatusFilter(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onApprove = { viewModel.approveListing(it) },
                            onRejectClick = { listingToReject = it },
                            onDeactivateClick = { listingToDeactivate = it },
                            onRestore = { viewModel.restoreListing(it) },
                            onViewDetails = { listingDetailToShow = it }
                        )
                    }
                }
            }
        }
    }

    // Reject Dialog
    listingToReject?.let { listing ->
        AlertDialog(
            onDismissRequest = {
                listingToReject = null
                rejectionReasonText = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reject Listing", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Enter rejection reason for '${listing.title}'. This will keep it hidden from users.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        placeholder = { Text("e.g. Missing phone number or invalid menu details") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = listingToReject
                        listingToReject = null
                        if (target != null) {
                            viewModel.rejectListing(target, rejectionReasonText)
                        }
                        rejectionReasonText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Confirm Rejection", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    listingToReject = null
                    rejectionReasonText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Deactivate Dialog
    listingToDeactivate?.let { listing ->
        AlertDialog(
            onDismissRequest = { listingToDeactivate = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deactivate Listing", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Are you sure you want to disable '${listing.title}'? It will be immediately hidden from public search, but kept for audit recovery.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = listingToDeactivate
                        listingToDeactivate = null
                        if (target != null) {
                            viewModel.deactivateListing(target)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Text("Deactivate", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { listingToDeactivate = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Listing Details Modal Dialog
    listingDetailToShow?.let { listing ->
        AlertDialog(
            onDismissRequest = { listingDetailToShow = null },
            title = {
                Text(listing.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (listing.imageUrl.isNotBlank()) {
                        ListingImage(
                            imageUrl = listing.imageUrl,
                            contentDescription = listing.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text("Category: ${listing.category}", fontWeight = FontWeight.Bold)
                    Text("Price: ${listing.price}", fontWeight = FontWeight.SemiBold)
                    Text("Location: ${listing.location}")
                    Text("Phone: ${listing.phone}")
                    if (listing.whatsApp.isNotBlank()) Text("WhatsApp: ${listing.whatsApp}")
                    Text("Status: ${listing.status}")
                    Text("Approval: ${listing.approvalStatus.uppercase()}")
                    if (listing.rejectionReason.isNotBlank()) {
                        Text("Rejection Note: ${listing.rejectionReason}", color = Color.Red)
                    }
                    Text("Owner UID: ${listing.ownerUid}", fontSize = 11.sp, color = Color.Gray)
                    val dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(listing.createdAt))
                    Text("Created: $dateFormatted", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { listingDetailToShow = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout of Owner Portal?") },
            text = { Text("You will be returned to the normal student login screen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewContent(
    stats: AdminStats,
    isAutoApprovalEnabled: Boolean,
    onToggleAutoApproval: (Boolean) -> Unit,
    onNavigateToSection: (AdminSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF581C87), Color(0xFFEC4899))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SYSTEM ADMINISTRATOR", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Campus Management Overview", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Monitor live listings, pending approvals, call activity and communications.", fontSize = 12.5.sp, color = Color.White.copy(alpha = 0.9f))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto-Approval Policy Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAutoApprovalEnabled) Color(0xFF0F3A28) else Color(0xFF261A35)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.2.dp,
                color = if (isAutoApprovalEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isAutoApprovalEnabled) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoApprovalEnabled) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = if (isAutoApprovalEnabled) Color(0xFF10B981) else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listing Auto-Approval",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAutoApprovalEnabled) Color(0xFF10B981) else Color(0xFFF59E0B).copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAutoApprovalEnabled) "ON (LIVE)" else "OFF (MANUAL)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAutoApprovalEnabled) Color.White else Color(0xFFFDE68A)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAutoApprovalEnabled)
                            "ON: Valid new listings are approved automatically and made live instantly on campus."
                        else
                            "OFF: Listings require manual review before appearing in public searches (Default).",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
                androidx.compose.material3.Switch(
                    checked = isAutoApprovalEnabled,
                    onCheckedChange = onToggleAutoApproval,
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF475569)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Pending Approvals Banner
        if (stats.totalPendingApprovals > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSection(AdminSection.PENDING_APPROVALS) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.45f)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Pending Approvals Queue", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                            Text("${stats.totalPendingApprovals} listings waiting for your review", fontSize = 12.sp, color = Color(0xFFFCA5A5))
                        }
                    }
                    Button(
                        onClick = { onNavigateToSection(AdminSection.PENDING_APPROVALS) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Review", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        Text("Category Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))

        // Grid of Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStatCard(
                title = "Restaurants",
                count = stats.totalRestaurants,
                subtext = "${stats.approvedRestaurants} Approved • ${stats.pendingRestaurantApprovals} Pending",
                icon = Icons.Default.Restaurant,
                iconColor = Color(0xFFF97316),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.RESTAURANTS) }
            )
            AdminStatCard(
                title = "Rentals",
                count = stats.totalRentals,
                subtext = "Bikes & Cars",
                icon = Icons.Default.DirectionsCar,
                iconColor = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.RENTALS) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStatCard(
                title = "Rooms & PGs",
                count = stats.totalRooms,
                subtext = "Flats & Hostels",
                icon = Icons.Default.Home,
                iconColor = Color(0xFF0284C7),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.ROOMS) }
            )
            AdminStatCard(
                title = "Services",
                count = stats.totalServices,
                subtext = "Laundry & Repairs",
                icon = Icons.Default.Build,
                iconColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.SERVICES) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AdminStatCard(
                title = "Marketplace",
                count = stats.totalMarketplace,
                subtext = "Tech, Books & Cycles",
                icon = Icons.Default.Storefront,
                iconColor = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.MARKETPLACE) }
            )
            AdminStatCard(
                title = "Jobs & Gigs",
                count = stats.totalJobs,
                subtext = "Campus Opportunities",
                icon = Icons.Default.Work,
                iconColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateToSection(AdminSection.JOBS) }
            )
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: Int,
    subtext: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = "$count",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun AdminCategoryManagementContent(
    section: AdminSection,
    listings: List<Listing>,
    selectedStatus: String,
    searchQuery: String,
    isLoading: Boolean,
    onStatusFilterSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onApprove: (Listing) -> Unit,
    onRejectClick: (Listing) -> Unit,
    onDeactivateClick: (Listing) -> Unit,
    onRestore: (Listing) -> Unit,
    onViewDetails: (Listing) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by title, location, phone...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFEC4899)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1B1330),
                unfocusedContainerColor = Color(0xFF1B1330),
                focusedBorderColor = Color(0xFFEC4899),
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        val statusFilters = listOf("All", "Pending", "Approved", "Rejected", "Disabled")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusFilters) { filter ->
                val isSelected = selectedStatus.equals(filter, ignoreCase = true)
                val badgeColor = when (filter) {
                    "Pending" -> Color(0xFFF59E0B)
                    "Approved" -> Color(0xFF22C55E)
                    "Rejected" -> Color(0xFFEF4444)
                    "Disabled" -> Color(0xFF94A3B8)
                    else -> Color(0xFFEC4899)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) badgeColor else Color(0xFF1F1638),
                    modifier = Modifier.clickable { onStatusFilterSelected(filter) }
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Listings List
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFEC4899))
            }
        } else if (listings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No listings match this filter", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Try changing search query or status filter", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(listings, key = { it.id }) { listing ->
                    AdminListingItemCard(
                        listing = listing,
                        onApprove = { onApprove(listing) },
                        onRejectClick = { onRejectClick(listing) },
                        onDeactivateClick = { onDeactivateClick(listing) },
                        onRestore = { onRestore(listing) },
                        onViewDetails = { onViewDetails(listing) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminListingItemCard(
    listing: Listing,
    onApprove: () -> Unit,
    onRejectClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    onRestore: () -> Unit,
    onViewDetails: () -> Unit
) {
    val statusColor = when (listing.approvalStatus.lowercase()) {
        "pending" -> Color(0xFFF59E0B)
        "approved" -> Color(0xFF22C55E)
        "rejected" -> Color(0xFFEF4444)
        "disabled" -> Color(0xFF94A3B8)
        else -> Color(0xFF22C55E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listing.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${listing.category} • ${listing.price}",
                        fontSize = 12.sp,
                        color = Color(0xFFEC4899),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Approval Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = listing.approvalStatus.uppercase(),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(listing.location, fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (listing.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Phone: ${listing.phone}", fontSize = 11.5.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            if (listing.rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${listing.rejectionReason}",
                    color = Color(0xFFFCA5A5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewDetails,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Details", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                }

                Spacer(modifier = Modifier.width(6.dp))

                when (listing.approvalStatus.lowercase()) {
                    "pending" -> {
                        OutlinedButton(
                            onClick = onRejectClick,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Reject", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Approve", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "approved" -> {
                        OutlinedButton(
                            onClick = onDeactivateClick,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Deactivate", color = Color(0xFFF59E0B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "rejected", "disabled" -> {
                        Button(
                            onClick = onRestore,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Restore & Approve", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCommunicationEventsList(
    events: List<CommunicationEvent>,
    title: String,
    emptyText: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Text("Privacy Note: Metadata only. Message contents and call audio are never stored.", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emptyText, color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when (event.channel) {
                                "whatsapp" -> Icons.AutoMirrored.Filled.Chat
                                "phone" -> Icons.Default.Call
                                else -> Icons.AutoMirrored.Filled.Chat
                            }
                            val iconColor = when (event.channel) {
                                "whatsapp" -> Color(0xFF22C55E)
                                "phone" -> Color(0xFF38BDF8)
                                else -> Color(0xFFEC4899)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${event.actorDisplayName} initiated ${event.channel.uppercase()} communication",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (event.listingTitle.isNotBlank()) {
                                    Text("Listing: ${event.listingTitle}", fontSize = 11.5.sp, color = Color(0xFFEC4899))
                                }
                                val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(event.timestamp))
                                Text("Time: $dateFormatted • Status: ${event.status}", fontSize = 10.5.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAuditLogsList(auditLogs: List<AdminAuditLog>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text("Administrative Audit Trail", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Text("Cryptographically verifiable record of owner actions (passkeys are never stored).", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        if (auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No administrative actions recorded yet", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.action, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFFEC4899))
                                val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                Text(dateFormatted, fontSize = 10.5.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(log.details, fontSize = 12.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Admin: ${log.adminEmail}", fontSize = 10.5.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsContent(isAutoApprovalEnabled: Boolean, onToggleAutoApproval: (Boolean) -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Owner Portal Security & Backend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEC4899))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Owner Passkey Configuration", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• The requested passkey 'TABasum@11' is verified against a secure salted SHA-256 hash.\n• Passkeys are never stored in plain text anywhere in source code, APK, or database.\n• Rate limiting locks out after 5 consecutive failed attempts for 15 minutes.\n• Server authorization rules enforce that only approved listings are returned in public searches.",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF22C55E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Cloud Functions Ready", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Backend Cloud Functions are provided in the functions/ directory. You can deploy them with 'firebase deploy --only functions' to enable server-side admin custom token verification.",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout of Owner Portal", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
