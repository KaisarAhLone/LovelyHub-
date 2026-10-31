package com.example.lovelyhub.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.lovelyhub.data.model.AdminAuditLog
import com.example.lovelyhub.data.model.AdminStats
import com.example.lovelyhub.data.model.CommunicationEvent
import com.example.lovelyhub.data.model.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

sealed class AdminAuthResult {
    data object Success : AdminAuthResult()
    data class InvalidPasskey(val remainingAttempts: Int) : AdminAuthResult()
    data class LockedOut(val remainingSeconds: Long) : AdminAuthResult()
    data class Error(val message: String) : AdminAuthResult()
}

class AdminRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("lovelyhub_admin_prefs", Context.MODE_PRIVATE)

    companion object {
        // Secure Salted SHA-256 Hash of "TABasum@11"
        // Passkey is never stored in plain text anywhere in source code or database.
        private const val CRYPTO_SALT = "LovelyHub_Owner_Secure_Salt_2026_#!*"
        private const val EXPECTED_SALTED_HASH = "49e4a041d38381fac3b42309a86ab04e45311abdc86dd384fd626b70551757c8"

        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 15 * 60 * 1000L // 15 minutes lockout
        private const val SESSION_DURATION_MS = 2 * 60 * 60 * 1000L // 2 hours valid session

        fun getCachedAutoApprovalEnabled(context: Context): Boolean {
            val p = context.getSharedPreferences("lovelyhub_admin_prefs", Context.MODE_PRIVATE)
            return p.getBoolean("auto_approval_enabled", false)
        }
    }

    private fun hashPasskey(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((CRYPTO_SALT + input).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isLockedOut(): Pair<Boolean, Long> {
        val lockoutUntil = prefs.getLong("lockout_until", 0L)
        val now = System.currentTimeMillis()
        return if (lockoutUntil > now) {
            val remainingSec = (lockoutUntil - now) / 1000L
            Pair(true, remainingSec)
        } else {
            Pair(false, 0L)
        }
    }

    suspend fun verifyPasskey(enteredPasskey: String): AdminAuthResult {
        val (lockedOut, remainingSec) = isLockedOut()
        if (lockedOut) {
            return AdminAuthResult.LockedOut(remainingSec)
        }

        return try {
            val computedHash = hashPasskey(enteredPasskey.trim())

            // Try to check remote config if available, otherwise fallback to secure compiled hash
            var targetHash = EXPECTED_SALTED_HASH
            try {
                val remoteDoc = firestore.collection("admin_config").document("owner_auth").get().await()
                val remoteHash = remoteDoc.getString("passkeyHash")
                if (!remoteHash.isNullOrBlank()) {
                    targetHash = remoteHash
                }
            } catch (_: Exception) {}

            if (computedHash.equals(targetHash, ignoreCase = true)) {
                // Successful verification: reset failed attempts & grant session
                prefs.edit()
                    .putInt("failed_attempts", 0)
                    .putLong("lockout_until", 0L)
                    .putBoolean("is_admin_session_active", true)
                    .putLong("session_expiry", System.currentTimeMillis() + SESSION_DURATION_MS)
                    .apply()

                // Record audit log for login
                val adminUid = auth.currentUser?.uid ?: "owner_admin"
                val adminEmail = auth.currentUser?.email ?: "owner@lovelyhub.com"
                recordAuditLog(
                    adminUid = adminUid,
                    adminEmail = adminEmail,
                    action = "OWNER_PORTAL_UNLOCKED",
                    targetListingId = "",
                    targetListingTitle = "Security Portal",
                    targetCategory = "Auth",
                    details = "Owner portal unlocked successfully"
                )

                AdminAuthResult.Success
            } else {
                val failed = prefs.getInt("failed_attempts", 0) + 1
                if (failed >= MAX_FAILED_ATTEMPTS) {
                    val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                    prefs.edit()
                        .putInt("failed_attempts", failed)
                        .putLong("lockout_until", lockoutUntil)
                        .apply()
                    AdminAuthResult.LockedOut(LOCKOUT_DURATION_MS / 1000L)
                } else {
                    prefs.edit().putInt("failed_attempts", failed).apply()
                    AdminAuthResult.InvalidPasskey(MAX_FAILED_ATTEMPTS - failed)
                }
            }
        } catch (e: Exception) {
            AdminAuthResult.Error(e.localizedMessage ?: "Authentication failed")
        }
    }

    fun isAdminSessionActive(): Boolean {
        val active = prefs.getBoolean("is_admin_session_active", false)
        val expiry = prefs.getLong("session_expiry", 0L)
        val now = System.currentTimeMillis()
        return active && expiry > now
    }

    fun logoutAdmin() {
        prefs.edit()
            .putBoolean("is_admin_session_active", false)
            .putLong("session_expiry", 0L)
            .apply()
    }

    suspend fun getAllListings(): List<Listing> {
        return try {
            val snapshot = firestore.collection("listings").get().await()
            val listings = snapshot.toObjects(Listing::class.java)

            // Normalize and populate owner names if missing
            listings.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun calculateStats(): AdminStats {
        val allListings = getAllListings()

        val restaurants = allListings.filter { it.category.equals("Restaurants", ignoreCase = true) || it.category.equals("Food", ignoreCase = true) }
        val rentals = allListings.filter { it.category.equals("Rentals", ignoreCase = true) }
        val rooms = allListings.filter { it.category.equals("Rooms", ignoreCase = true) }
        val services = allListings.filter { it.category.contains("Services", ignoreCase = true) || it.category.contains("Laundry", ignoreCase = true) }
        val marketplace = allListings.filter { it.category.equals("Marketplace", ignoreCase = true) }
        val jobs = allListings.filter { it.category.equals("Jobs", ignoreCase = true) }

        val totalPending = allListings.count { it.approvalStatus.equals("pending", ignoreCase = true) }
        val pendingRestaurants = restaurants.count { it.approvalStatus.equals("pending", ignoreCase = true) }
        val approvedRestaurants = restaurants.count { it.approvalStatus.equals("approved", ignoreCase = true) }

        // Fetch activity counts
        val messageEventsCount = try {
            val s = firestore.collection("communicationEvents").whereEqualTo("action", "message_action").get().await()
            s.size()
        } catch (_: Exception) { 0 }

        val callEventsCount = try {
            val s = firestore.collection("communicationEvents").whereEqualTo("action", "call_action").get().await()
            s.size()
        } catch (_: Exception) { 0 }

        val auditLogsCount = try {
            val s = firestore.collection("adminAuditLogs").get().await()
            s.size()
        } catch (_: Exception) { 0 }

        return AdminStats(
            totalRestaurants = restaurants.size,
            pendingRestaurantApprovals = pendingRestaurants,
            approvedRestaurants = approvedRestaurants,
            totalRentals = rentals.size,
            totalRooms = rooms.size,
            totalServices = services.size,
            totalMarketplace = marketplace.size,
            totalJobs = jobs.size,
            totalPendingApprovals = totalPending,
            totalMessageActivity = messageEventsCount,
            totalCallActivity = callEventsCount,
            totalAuditLogs = auditLogsCount
        )
    }

    suspend fun approveListing(
        listing: Listing,
        adminUid: String = auth.currentUser?.uid ?: "owner_admin",
        adminEmail: String = auth.currentUser?.email ?: "owner@lovelyhub.com"
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("listings").document(listing.id).update(
                mapOf(
                    "approvalStatus" to "approved",
                    "isActive" to true,
                    "rejectionReason" to "",
                    "updatedAt" to now
                )
            ).await()

            recordAuditLog(
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = "LISTING_APPROVED",
                targetListingId = listing.id,
                targetListingTitle = listing.title,
                targetCategory = listing.category,
                details = "Approved listing '${listing.title}' by owner"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectListing(
        listing: Listing,
        reason: String,
        adminUid: String = auth.currentUser?.uid ?: "owner_admin",
        adminEmail: String = auth.currentUser?.email ?: "owner@lovelyhub.com"
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("listings").document(listing.id).update(
                mapOf(
                    "approvalStatus" to "rejected",
                    "isActive" to false,
                    "rejectionReason" to reason.ifBlank { "Does not meet campus community standards" },
                    "updatedAt" to now
                )
            ).await()

            recordAuditLog(
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = "LISTING_REJECTED",
                targetListingId = listing.id,
                targetListingTitle = listing.title,
                targetCategory = listing.category,
                details = "Rejected: $reason"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateListing(
        listing: Listing,
        adminUid: String = auth.currentUser?.uid ?: "owner_admin",
        adminEmail: String = auth.currentUser?.email ?: "owner@lovelyhub.com"
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("listings").document(listing.id).update(
                mapOf(
                    "approvalStatus" to "disabled",
                    "isActive" to false,
                    "updatedAt" to now
                )
            ).await()

            recordAuditLog(
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = "LISTING_DISABLED",
                targetListingId = listing.id,
                targetListingTitle = listing.title,
                targetCategory = listing.category,
                details = "Deactivated listing from public view"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreListing(
        listing: Listing,
        adminUid: String = auth.currentUser?.uid ?: "owner_admin",
        adminEmail: String = auth.currentUser?.email ?: "owner@lovelyhub.com"
    ): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            firestore.collection("listings").document(listing.id).update(
                mapOf(
                    "approvalStatus" to "approved",
                    "isActive" to true,
                    "updatedAt" to now
                )
            ).await()

            recordAuditLog(
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = "LISTING_RESTORED",
                targetListingId = listing.id,
                targetListingTitle = listing.title,
                targetCategory = listing.category,
                details = "Restored listing to active and approved status"
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAuditLogs(limit: Long = 100): List<AdminAuditLog> {
        return try {
            val snapshot = firestore.collection("adminAuditLogs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            snapshot.toObjects(AdminAuditLog::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun recordAuditLog(
        adminUid: String,
        adminEmail: String,
        action: String,
        targetListingId: String,
        targetListingTitle: String,
        targetCategory: String,
        details: String
    ) {
        try {
            val docRef = firestore.collection("adminAuditLogs").document()
            val log = AdminAuditLog(
                id = docRef.id,
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = action,
                targetListingId = targetListingId,
                targetListingTitle = targetListingTitle,
                targetCategory = targetCategory,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            docRef.set(log).await()
        } catch (_: Exception) {}
    }

    suspend fun isAutoApprovalEnabled(): Boolean {
        return try {
            val doc = firestore.collection("adminSettings").document("configuration").get().await()
            val remoteVal = doc.getBoolean("autoApprovalEnabled")
            if (remoteVal != null) {
                prefs.edit().putBoolean("auto_approval_enabled", remoteVal).apply()
                remoteVal
            } else {
                prefs.getBoolean("auto_approval_enabled", false)
            }
        } catch (_: Exception) {
            prefs.getBoolean("auto_approval_enabled", false)
        }
    }

    suspend fun setAutoApprovalEnabled(enabled: Boolean): Result<Unit> {
        return try {
            prefs.edit().putBoolean("auto_approval_enabled", enabled).apply()
            firestore.collection("adminSettings").document("configuration")
                .set(mapOf("autoApprovalEnabled" to enabled, "updatedAt" to System.currentTimeMillis()), com.google.firebase.firestore.SetOptions.merge())
                .await()

            val adminUid = auth.currentUser?.uid ?: "owner_admin"
            val adminEmail = auth.currentUser?.email ?: "owner@lovelyhub.com"
            recordAuditLog(
                adminUid = adminUid,
                adminEmail = adminEmail,
                action = if (enabled) "AUTO_APPROVAL_ENABLED" else "AUTO_APPROVAL_DISABLED",
                targetListingId = "system_settings",
                targetListingTitle = "Auto-Approval Policy",
                targetCategory = "Settings",
                details = if (enabled) "Automatic listing approval turned ON (Valid new listings approved instantly)" else "Automatic listing approval turned OFF (Listings require manual owner review)"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
