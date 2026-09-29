package com.example.lovelyhub.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.model.UserRole
import com.example.lovelyhub.ui.auth.AuthViewModel
import com.example.lovelyhub.ui.components.ListingImage
import com.example.lovelyhub.ui.listings.ListingViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: AuthViewModel,
    listingViewModel: ListingViewModel = viewModel(),
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.currentUserProfile.collectAsState()
    val currentUser = viewModel.currentUser
    val userListings by listingViewModel.userListings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf(UserRole.STUDENT) }
    var isRoleExpanded by remember { mutableStateOf(false) }

    var itemToEdit by remember { mutableStateOf<Listing?>(null) }
    var editItemTitle by remember { mutableStateOf("") }
    var editItemPrice by remember { mutableStateOf("") }
    var editItemPhone by remember { mutableStateOf("") }
    var editItemImageUri by remember { mutableStateOf<Uri?>(null) }

    var itemToDelete by remember { mutableStateOf<Listing?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val editItemImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            editItemImageUri = uri
        }
    }

    LaunchedEffect(Unit) {
        listingViewModel.fetchUserListings()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            viewModel.updateUserProfile(
                name = userProfile?.name ?: currentUser?.displayName ?: "User",
                email = userProfile?.email ?: currentUser?.email ?: "",
                role = userProfile?.role ?: "Student",
                photoUrl = uri.toString()
            ) { error ->
                if (error != null) {
                    scope.launch { snackbarHostState.showSnackbar(error) }
                } else {
                    scope.launch { snackbarHostState.showSnackbar("Profile picture updated!") }
                }
            }
        }
    }

    val currentPhotoUrl = selectedImageUri?.toString()
        ?: userProfile?.photoUrl?.ifBlank { null }
        ?: currentUser?.photoUrl?.toString()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF6F7FB)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            if (currentPhotoUrl != null) {
                                AsyncImage(
                                    model = currentPhotoUrl,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color(0xFF7926E1), CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF0E5FC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF7926E1),
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7926E1))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Upload Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = userProfile?.name ?: currentUser?.displayName ?: "Campus User",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E2D)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = userProfile?.email ?: currentUser?.email ?: "",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF7926E1).copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = userProfile?.role ?: "Student",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7926E1)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                editName = userProfile?.name ?: currentUser?.displayName ?: ""
                                editEmail = userProfile?.email ?: currentUser?.email ?: ""
                                editRole = UserRole.entries.find { it.displayName == userProfile?.role } ?: UserRole.STUDENT
                                showEditProfileDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7926E1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile Details", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "My Uploaded Items",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E2D)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (userListings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "You haven't uploaded any items yet.",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            userListings.forEachIndexed { index, listing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (listing.imageUrl.isNotBlank()) {
                                        ListingImage(
                                            imageUrl = listing.imageUrl,
                                            contentDescription = listing.title,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = listing.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E1E2D)
                                        )
                                        Text(
                                            text = "${listing.category} • ${listing.price}",
                                            fontSize = 13.sp,
                                            color = Color(0xFF7926E1),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            itemToEdit = listing
                                            editItemTitle = listing.title
                                            editItemPrice = listing.price
                                            editItemPhone = listing.phone
                                            editItemImageUri = null
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Item",
                                                tint = Color(0xFF7926E1),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        IconButton(onClick = { itemToDelete = listing }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Item",
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < userListings.size - 1) {
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Account & Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E2D)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ProfileOptionItem("My Favorites", Icons.Default.Favorite)
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        ProfileOptionItem("App Settings", Icons.Default.Settings)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }

    if (itemToEdit != null) {
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = { Text("Edit Listing Item", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0E5FC))
                            .clickable { editItemImagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (editItemImageUri != null) {
                            AsyncImage(
                                model = editItemImageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (itemToEdit?.imageUrl?.isNotBlank() == true) {
                            ListingImage(
                                imageUrl = itemToEdit!!.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("Tap to change photo", color = Color(0xFF7926E1), fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editItemTitle,
                        onValueChange = { editItemTitle = it },
                        label = { Text("Name / Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editItemPrice,
                        onValueChange = { editItemPrice = it },
                        label = { Text("Price") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editItemPhone,
                        onValueChange = { editItemPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val current = itemToEdit
                        itemToEdit = null
                        if (current != null) {
                            val updated = current.copy(
                                title = editItemTitle,
                                price = editItemPrice,
                                phone = editItemPhone
                            )
                            listingViewModel.updateListing(context, updated, editItemImageUri) {
                                scope.launch { snackbarHostState.showSnackbar("Listing updated successfully!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7926E1))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Mark as Sold / Delete", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${itemToDelete?.title}'? It will no longer be visible to other users.") },
            confirmButton = {
                Button(
                    onClick = {
                        val item = itemToDelete
                        itemToDelete = null
                        if (item != null) {
                            listingViewModel.deleteListing(item.id, item.category) {
                                scope.launch { snackbarHostState.showSnackbar("Item marked as sold / deleted!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editRole.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = {
                                IconButton(onClick = { isRoleExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = isRoleExpanded,
                            onDismissRequest = { isRoleExpanded = false }
                        ) {
                            UserRole.entries.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.displayName) },
                                    onClick = {
                                        editRole = role
                                        isRoleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            name = editName,
                            email = editEmail,
                            role = editRole.displayName,
                            photoUrl = selectedImageUri?.toString() ?: userProfile?.photoUrl ?: ""
                        ) { error ->
                            showEditProfileDialog = false
                            if (error != null) {
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Profile updated successfully!") }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7926E1))
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileOptionItem(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = Color(0xFF7926E1), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
    }
}
