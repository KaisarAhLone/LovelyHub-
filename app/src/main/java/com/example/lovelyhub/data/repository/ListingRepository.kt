package com.example.lovelyhub.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.lovelyhub.data.model.Listing
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ListingRepository {

    private val firestore = FirebaseFirestore.getInstance()
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

    suspend fun getListings(category: String): List<Listing> {
        val remoteItems = try {
            val snapshot = firestore.collection("listings").get().await()
            snapshot.toObjects(Listing::class.java)
        } catch (e: Exception) {
            emptyList()
        }

        val allItems = (memoryListings + remoteItems)
            .distinctBy { if (it.id.isNotBlank()) it.id else "${it.title}_${it.createdAt}" }

        return allItems
            .filter { it.category.equals(category, ignoreCase = true) }
            .sortedByDescending { it.createdAt }
    }

    suspend fun addListing(context: Context, listing: Listing, imageUri: Uri?): Result<Unit> {
        val encodedImage = if (imageUri != null) {
            compressAndEncodeImage(context, imageUri)
        } else {
            listing.imageUrl
        }

        val docRef = firestore.collection("listings").document()
        val finalListing = listing.copy(
            id = if (listing.id.isBlank()) docRef.id else listing.id,
            imageUrl = encodedImage
        )

        memoryListings.add(0, finalListing)

        try {
            docRef.set(finalListing).await()
        } catch (_: Exception) {}

        return Result.success(Unit)
    }
}
