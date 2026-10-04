package com.example.lovelyhub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.lovelyhub.data.model.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ListingRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val memoryListings = mutableListOf<Listing>()

    suspend fun compressAndEncodeImage(context: Context, imageUri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return ""
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return ""

            val maxDimension = 600
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = if (width > height) {
                maxDimension.toFloat() / width
            } else {
                maxDimension.toFloat() / height
            }

            val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
            val scaledHeight = (height * scale).toInt().coerceAtLeast(1)

            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val imageBytes = outputStream.toByteArray()

            val base64String = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64String"
        } catch (e: Exception) {
            ""
        }
    }

    private suspend fun getFavoriteIds(): Set<String> {
        val uid = auth.currentUser?.uid ?: return emptySet()
        return try {
            val snapshot = firestore.collection("users").document(uid).collection("favorites").get().await()
            snapshot.documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    suspend fun getListings(category: String): List<Listing> {
        val favoriteIds = getFavoriteIds()
        val remoteItems = try {
            val snapshot = firestore.collection("listings").get().await()
            snapshot.toObjects(Listing::class.java)
        } catch (e: Exception) {
            emptyList()
        }

        val allItems = (memoryListings + remoteItems)
            .distinctBy { if (it.id.isNotBlank()) it.id else "${it.title}_${it.createdAt}" }

        val filtered = if (category.equals("Favorites", ignoreCase = true)) {
            allItems.filter { favoriteIds.contains(it.id) || it.isFavorite }
        } else if (category.equals("Restaurants", ignoreCase = true) || category.equals("Food", ignoreCase = true)) {
            allItems.filter { it.category.equals("Restaurants", ignoreCase = true) || it.category.equals("Food", ignoreCase = true) }
        } else {
            allItems.filter { it.category.equals(category, ignoreCase = true) }
        }

        return filtered
            .map { listing -> listing.copy(isFavorite = favoriteIds.contains(listing.id)) }
            .sortedWith(
                compareByDescending<Listing> { it.status.equals("Open", ignoreCase = true) }
                    .thenByDescending { it.createdAt }
            )
    }

    suspend fun getUserListings(): List<Listing> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val remoteItems = try {
            val snapshot = firestore.collection("listings").get().await()
            snapshot.toObjects(Listing::class.java)
        } catch (e: Exception) {
            emptyList()
        }

        val allItems = (memoryListings + remoteItems)
            .distinctBy { if (it.id.isNotBlank()) it.id else "${it.title}_${it.createdAt}" }

        return allItems
            .filter { it.ownerUid == uid }
            .sortedByDescending { it.createdAt }
    }

    suspend fun addListing(context: Context, listing: Listing, imageUri: Uri?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: ""
        val encodedImage = if (imageUri != null) {
            compressAndEncodeImage(context, imageUri)
        } else {
            listing.imageUrl
        }

        val isBusinessCategory = listing.category.equals("Restaurants", ignoreCase = true) ||
                listing.category.equals("Food", ignoreCase = true) ||
                listing.category.equals("Rooms", ignoreCase = true) ||
                listing.category.equals("Rentals", ignoreCase = true)

        if (isBusinessCategory && uid.isNotBlank()) {
            val dishEntry = if (encodedImage.isNotBlank()) {
                "${listing.title} - ${listing.price}|$encodedImage"
            } else {
                "${listing.title} - ${listing.price}"
            }

            try {
                val shopDocRef = firestore.collection("listings").document(uid)
                val doc = shopDocRef.get().await()
                if (doc.exists()) {
                    shopDocRef.update("menuItems", FieldValue.arrayUnion(dishEntry)).await()
                } else {
                    val newShop = listing.copy(
                        id = uid,
                        ownerUid = uid,
                        imageUrl = encodedImage,
                        menuItems = listOf(dishEntry),
                        isShop = true
                    )
                    shopDocRef.set(newShop).await()
                    memoryListings.add(0, newShop)
                }

                val index = memoryListings.indexOfFirst { it.id == uid }
                if (index != -1) {
                    val existing = memoryListings[index]
                    val updatedList = existing.menuItems + dishEntry
                    memoryListings[index] = existing.copy(menuItems = updatedList)
                }
            } catch (_: Exception) {}
        } else {
            val docRef = firestore.collection("listings").document()
            val finalListing = listing.copy(
                id = if (listing.id.isBlank()) docRef.id else listing.id,
                imageUrl = encodedImage,
                ownerUid = uid
            )
            memoryListings.add(0, finalListing)
            try {
                docRef.set(finalListing).await()
            } catch (_: Exception) {}
        }

        return Result.success(Unit)
    }

    suspend fun updateListing(context: Context, listing: Listing, newImageUri: Uri?): Result<Unit> {
        return try {
            val finalImage = if (newImageUri != null) {
                compressAndEncodeImage(context, newImageUri)
            } else {
                listing.imageUrl
            }

            val updatedListing = listing.copy(imageUrl = finalImage)

            val index = memoryListings.indexOfFirst { it.id == listing.id }
            if (index != -1) {
                memoryListings[index] = updatedListing
            }

            val updates = mapOf(
                "title" to updatedListing.title,
                "price" to updatedListing.price,
                "phone" to updatedListing.phone,
                "category" to updatedListing.category,
                "imageUrl" to updatedListing.imageUrl,
                "status" to updatedListing.status
            )

            firestore.collection("listings").document(listing.id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        memoryListings.removeAll { it.id == listingId }
        return try {
            firestore.collection("listings").document(listingId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.success(Unit)
        }
    }

    suspend fun toggleFavorite(listingId: String, currentFavoriteState: Boolean): Result<Boolean> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        val favRef = firestore.collection("users").document(uid).collection("favorites").document(listingId)
        val newFavState = !currentFavoriteState

        return try {
            if (newFavState) {
                favRef.set(mapOf("timestamp" to System.currentTimeMillis())).await()
            } else {
                favRef.delete().await()
            }
            Result.success(newFavState)
        } catch (e: Exception) {
            Result.success(newFavState)
        }
    }
}
