package com.example.lovelyhub.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.lovelyhub.data.local.LocalUserCache
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.File

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val listingRepository: ListingRepository = ListingRepository()
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(
        context: Context,
        name: String,
        email: String,
        password: String,
        role: String,
        businessName: String = "",
        businessPhone: String = "",
        businessWhatsApp: String = "",
        businessLocation: String = "",
        openTime: String = "09:00 AM",
        closeTime: String = "10:00 PM",
        is24Hours: Boolean = false,
        imageUri: Uri? = null
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: return Result.failure(Exception("User creation failed"))

            val encodedPhoto = if (imageUri != null) {
                listingRepository.compressAndEncodeImage(context, imageUri)
            } else {
                ""
            }

            if (encodedPhoto.isNotBlank()) {
                LocalUserCache(context).saveProfilePhotoBase64(user.uid, encodedPhoto)
            }

            val finalWhatsApp = businessWhatsApp.ifBlank { businessPhone }

            val newUser = User(
                uid = user.uid,
                name = name,
                email = email,
                role = role,
                businessName = businessName,
                businessPhone = businessPhone,
                businessWhatsApp = finalWhatsApp,
                businessLocation = businessLocation,
                openTime = openTime,
                closeTime = closeTime,
                is24Hours = is24Hours,
                photoUrl = encodedPhoto
            )

            firestore.collection("users")
                .document(user.uid)
                .set(newUser)
                .await()

            if (role != "Student" && businessName.isNotBlank()) {
                val category = when (role) {
                    "Restaurant Owner" -> "Restaurants"
                    "Room / PG Owner" -> "Rooms"
                    "Rental Provider" -> "Rentals"
                    "Service Provider" -> "Services"
                    else -> "Marketplace"
                }

                val shopListing = Listing(
                    id = user.uid,
                    category = category,
                    title = businessName,
                    price = if (is24Hours) "Open 24 Hours" else "Open: $openTime - $closeTime",
                    phone = businessPhone,
                    whatsApp = finalWhatsApp,
                    location = businessLocation,
                    imageUrl = encodedPhoto,
                    openTime = if (is24Hours) "Open 24 Hours" else openTime,
                    closeTime = if (is24Hours) "Open 24 Hours" else closeTime,
                    is24Hours = is24Hours,
                    ownerUid = user.uid,
                    status = "Open",
                    isShop = true
                )

                try {
                    firestore.collection("listings").document(user.uid).set(shopListing).await()
                } catch (_: Exception) {}
            }

            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("An account with this email address ($email) already exists! Please use the Login tab to sign in."))
        } catch (e: Exception) {
            val msg = if (e.message?.contains("already in use", ignoreCase = true) == true) {
                "An account with this email address ($email) already exists! Please use the Login tab to sign in."
            } else {
                e.localizedMessage ?: "Registration failed"
            }
            Result.failure(Exception(msg))
        }
    }

    suspend fun signInWithGoogle(
        context: Context,
        webClientId: String,
        isRegistration: Boolean = false,
        name: String = "",
        role: String = "Student",
        businessName: String = "",
        businessPhone: String = "",
        businessWhatsApp: String = "",
        businessLocation: String = "",
        openTime: String = "09:00 AM",
        closeTime: String = "10:00 PM",
        is24Hours: Boolean = false,
        imageUri: Uri? = null
    ): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: return Result.failure(Exception("Firebase user is null"))

                val encodedPhoto = if (imageUri != null) {
                    listingRepository.compressAndEncodeImage(context, imageUri)
                } else {
                    user.photoUrl?.toString() ?: ""
                }

                val finalWhatsApp = businessWhatsApp.ifBlank { businessPhone }
                val finalName = name.ifBlank { user.displayName ?: googleIdTokenCredential.displayName ?: "Campus User" }

                val userDoc = firestore.collection("users").document(user.uid).get().await()

                if (isRegistration) {
                    if (userDoc.exists()) {
                        val existingUser = userDoc.toObject(User::class.java)
                        val existingRole = existingUser?.role ?: "a registered user"
                        auth.signOut()
                        return Result.failure(Exception("An account with email '${user.email}' already exists as '$existingRole'! Please use the Login tab to sign in."))
                    }

                    if (encodedPhoto.isNotBlank()) {
                        LocalUserCache(context).saveProfilePhotoBase64(user.uid, encodedPhoto)
                    }

                    val newUser = User(
                        uid = user.uid,
                        name = finalName,
                        email = user.email ?: "",
                        role = role,
                        businessName = businessName,
                        businessPhone = businessPhone,
                        businessWhatsApp = finalWhatsApp,
                        businessLocation = businessLocation,
                        openTime = openTime,
                        closeTime = closeTime,
                        is24Hours = is24Hours,
                        photoUrl = encodedPhoto
                    )
                    firestore.collection("users").document(user.uid).set(newUser).await()

                    if (role != "Student" && businessName.isNotBlank()) {
                        val category = when (role) {
                            "Restaurant Owner" -> "Restaurants"
                            "Room / PG Owner" -> "Rooms"
                            "Rental Provider" -> "Rentals"
                            else -> "Marketplace"
                        }

                        val shopListing = Listing(
                            id = user.uid,
                            category = category,
                            title = businessName,
                            price = if (is24Hours) "Open 24 Hours" else "Open: $openTime - $closeTime",
                            phone = businessPhone,
                            whatsApp = finalWhatsApp,
                            location = businessLocation,
                            imageUrl = encodedPhoto,
                            openTime = if (is24Hours) "Open 24 Hours" else openTime,
                            closeTime = if (is24Hours) "Open 24 Hours" else closeTime,
                            is24Hours = is24Hours,
                            ownerUid = user.uid,
                            status = "Open",
                            isShop = true
                        )

                        try {
                            firestore.collection("listings").document(user.uid).set(shopListing).await()
                        } catch (_: Exception) {}
                    }
                } else {
                    if (!userDoc.exists()) {
                        auth.signOut()
                        return Result.failure(Exception("No account found for '${user.email}'. Please use the Register tab to create an account."))
                    }
                }

                Result.success(user)
            } else {
                Result.failure(Exception("Unrecognized credential type"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(uid: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(
        uid: String,
        name: String,
        email: String,
        role: String,
        businessName: String,
        businessPhone: String,
        businessLocation: String,
        photoUrl: String,
        openTime: String = "09:00 AM",
        closeTime: String = "10:00 PM",
        is24Hours: Boolean = false
    ): Result<Unit> {
        return try {
            val finalPhotoStr = if (photoUrl.startsWith("/") || photoUrl.startsWith("file:")) {
                try {
                    val file = File(photoUrl)
                    if (file.exists()) {
                        val bytes = file.readBytes()
                        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        "data:image/jpeg;base64,$b64"
                    } else photoUrl
                } catch (_: Exception) { photoUrl }
            } else {
                photoUrl
            }

            val updates = mapOf(
                "name" to name,
                "email" to email,
                "role" to role,
                "businessName" to businessName,
                "businessPhone" to businessPhone,
                "businessLocation" to businessLocation,
                "photoUrl" to finalPhotoStr,
                "openTime" to (if (is24Hours) "Open 24 Hours" else openTime),
                "closeTime" to (if (is24Hours) "Open 24 Hours" else closeTime),
                "is24Hours" to is24Hours
            )
            firestore.collection("users").document(uid).update(updates).await()

            if (role != "Student" && businessName.isNotBlank()) {
                val category = when (role) {
                    "Restaurant Owner" -> "Restaurants"
                    "Room / PG Owner" -> "Rooms"
                    "Rental Provider" -> "Rentals"
                    else -> "Marketplace"
                }

                val shopUpdates = mutableMapOf<String, Any>(
                    "title" to businessName,
                    "phone" to businessPhone,
                    "location" to businessLocation,
                    "category" to category,
                    "isShop" to true,
                    "is24Hours" to is24Hours,
                    "openTime" to (if (is24Hours) "Open 24 Hours" else openTime),
                    "closeTime" to (if (is24Hours) "Open 24 Hours" else closeTime),
                    "price" to (if (is24Hours) "Open 24 Hours" else "Open: $openTime - $closeTime")
                )
                if (finalPhotoStr.isNotBlank()) {
                    shopUpdates["imageUrl"] = finalPhotoStr
                }

                try {
                    firestore.collection("listings").document(uid).update(shopUpdates).await()
                } catch (_: Exception) {
                    val shopListing = Listing(
                        id = uid,
                        category = category,
                        title = businessName,
                        price = if (is24Hours) "Open 24 Hours" else "Open: $openTime - $closeTime",
                        phone = businessPhone,
                        location = businessLocation,
                        imageUrl = finalPhotoStr,
                        openTime = if (is24Hours) "Open 24 Hours" else openTime,
                        closeTime = if (is24Hours) "Open 24 Hours" else closeTime,
                        is24Hours = is24Hours,
                        ownerUid = uid,
                        status = "Open",
                        isShop = true
                    )
                    firestore.collection("listings").document(uid).set(shopListing).await()
                }
            }

            val user = auth.currentUser
            if (user != null && email.isNotBlank() && email != user.email) {
                user.verifyBeforeUpdateEmail(email).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccount(context: Context): Result<Unit> {
        val user = auth.currentUser ?: run {
            auth.signOut()
            return Result.success(Unit)
        }
        val uid = user.uid

        try {
            firestore.collection("users").document(uid).delete().await()
        } catch (_: Exception) {}

        try {
            firestore.collection("listings").document(uid).delete().await()
        } catch (_: Exception) {}

        try {
            LocalUserCache(context).removeProfilePhoto(uid)
            LocalUserCache(context).setAcceptedDisclaimer(false, uid)
        } catch (_: Exception) {}

        try {
            user.delete().await()
        } catch (_: Exception) {}

        try {
            auth.signOut()
        } catch (_: Exception) {}

        return Result.success(Unit)
    }

    fun signOut() {
        auth.signOut()
    }
}
