package com.example.lovelyhub.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovelyhub.data.model.AdminAuditLog
import com.example.lovelyhub.data.model.AdminStats
import com.example.lovelyhub.data.model.CommunicationEvent
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.repository.AdminAuthResult
import com.example.lovelyhub.data.repository.AdminRepository
import com.example.lovelyhub.data.repository.CommunicationEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AdminAuthState {
    data object Unauthenticated : AdminAuthState()
    data object Loading : AdminAuthState()
    data object Authenticated : AdminAuthState()
    data class Error(val message: String) : AdminAuthState()
    data class LockedOut(val remainingSeconds: Long) : AdminAuthState()
}

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val adminRepository = AdminRepository(application.applicationContext)
    private val commRepository = CommunicationEventRepository()

    private val _authState = MutableStateFlow<AdminAuthState>(
        AdminAuthState.Unauthenticated // Always start unauthenticated - no session persistence
    )
    val authState: StateFlow<AdminAuthState> = _authState

    private val _adminStats = MutableStateFlow(AdminStats())
    val adminStats: StateFlow<AdminStats> = _adminStats

    private val _allListings = MutableStateFlow<List<Listing>>(emptyList())
    val allListings: StateFlow<List<Listing>> = _allListings

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedStatusFilter = MutableStateFlow("All") // "All", "Pending", "Approved", "Rejected", "Disabled"
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messageEvents = MutableStateFlow<List<CommunicationEvent>>(emptyList())
    val messageEvents: StateFlow<List<CommunicationEvent>> = _messageEvents

    private val _callEvents = MutableStateFlow<List<CommunicationEvent>>(emptyList())
    val callEvents: StateFlow<List<CommunicationEvent>> = _callEvents

    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage

    private val _isAutoApprovalEnabled = MutableStateFlow(false)
    val isAutoApprovalEnabled: StateFlow<Boolean> = _isAutoApprovalEnabled

    // Filtered listings based on category, approval status, and search query
    val filteredListings: StateFlow<List<Listing>> = combine(
        _allListings,
        _selectedCategory,
        _selectedStatusFilter,
        _searchQuery
    ) { listings, category, statusFilter, query ->
        listings.filter { listing ->
            // Category filter
            val matchCategory = when {
                category.equals("All", ignoreCase = true) -> true
                category.equals("Restaurants", ignoreCase = true) -> listing.category.equals("Restaurants", ignoreCase = true) || listing.category.equals("Food", ignoreCase = true)
                category.equals("Rentals", ignoreCase = true) -> listing.category.equals("Rentals", ignoreCase = true)
                category.equals("Rooms", ignoreCase = true) -> listing.category.equals("Rooms", ignoreCase = true)
                category.equals("Services", ignoreCase = true) -> listing.category.contains("Services", ignoreCase = true) || listing.category.contains("Laundry", ignoreCase = true)
                category.equals("Marketplace", ignoreCase = true) -> listing.category.equals("Marketplace", ignoreCase = true)
                category.equals("Jobs", ignoreCase = true) -> listing.category.equals("Jobs", ignoreCase = true)
                else -> listing.category.contains(category, ignoreCase = true)
            }

            // Status filter
            val matchStatus = when {
                statusFilter.equals("All", ignoreCase = true) -> true
                statusFilter.equals("Pending", ignoreCase = true) -> listing.approvalStatus.equals("pending", ignoreCase = true)
                statusFilter.equals("Approved", ignoreCase = true) -> listing.approvalStatus.equals("approved", ignoreCase = true) && listing.isActive
                statusFilter.equals("Rejected", ignoreCase = true) -> listing.approvalStatus.equals("rejected", ignoreCase = true)
                statusFilter.equals("Disabled", ignoreCase = true) -> listing.approvalStatus.equals("disabled", ignoreCase = true) || !listing.isActive
                else -> true
            }

            // Search query
            val matchQuery = if (query.isBlank()) true else {
                listing.title.contains(query, ignoreCase = true) ||
                        listing.location.contains(query, ignoreCase = true) ||
                        listing.phone.contains(query, ignoreCase = true) ||
                        listing.ownerUid.contains(query, ignoreCase = true)
            }

            matchCategory && matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkLockout()
        // No auto-restore session - portal requires passkey every time
        adminRepository.logoutAdmin() // Clear any leftover session on init
    }

    fun checkLockout() {
        val (lockedOut, remainingSec) = adminRepository.isLockedOut()
        if (lockedOut) {
            _authState.value = AdminAuthState.LockedOut(remainingSec)
        }
    }

    fun unlockPortal(passkey: String) {
        if (passkey.isBlank()) {
            _authState.value = AdminAuthState.Error("Please enter owner passkey")
            return
        }

        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            when (val result = adminRepository.verifyPasskey(passkey)) {
                is AdminAuthResult.Success -> {
                    _authState.value = AdminAuthState.Authenticated
                    refreshDashboard()
                }
                is AdminAuthResult.InvalidPasskey -> {
                    _authState.value = AdminAuthState.Error("Invalid passkey. Remaining attempts: ${result.remainingAttempts}")
                }
                is AdminAuthResult.LockedOut -> {
                    _authState.value = AdminAuthState.LockedOut(result.remainingSeconds)
                }
                is AdminAuthResult.Error -> {
                    _authState.value = AdminAuthState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        adminRepository.logoutAdmin()
        _authState.value = AdminAuthState.Unauthenticated
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _adminStats.value = adminRepository.calculateStats()
                _allListings.value = adminRepository.getAllListings()
                _messageEvents.value = commRepository.getEvents(channel = "whatsapp", limit = 60) + commRepository.getEvents(channel = "in_app", limit = 60)
                _callEvents.value = commRepository.getEvents(channel = "phone", limit = 100)
                _auditLogs.value = adminRepository.getAuditLogs(limit = 100)
                _isAutoApprovalEnabled.value = adminRepository.isAutoApprovalEnabled()
            } catch (_: Exception) {}
            finally {
                _isLoading.value = false
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun approveListing(listing: Listing) {
        viewModelScope.launch {
            val result = adminRepository.approveListing(listing)
            if (result.isSuccess) {
                _actionMessage.value = "Approved: ${listing.title}"
                refreshDashboard()
            } else {
                _actionMessage.value = "Failed to approve listing"
            }
        }
    }

    fun rejectListing(listing: Listing, reason: String) {
        viewModelScope.launch {
            val result = adminRepository.rejectListing(listing, reason)
            if (result.isSuccess) {
                _actionMessage.value = "Rejected: ${listing.title}"
                refreshDashboard()
            } else {
                _actionMessage.value = "Failed to reject listing"
            }
        }
    }

    fun deactivateListing(listing: Listing) {
        viewModelScope.launch {
            val result = adminRepository.deactivateListing(listing)
            if (result.isSuccess) {
                _actionMessage.value = "Deactivated: ${listing.title}"
                refreshDashboard()
            } else {
                _actionMessage.value = "Failed to deactivate listing"
            }
        }
    }

    fun restoreListing(listing: Listing) {
        viewModelScope.launch {
            val result = adminRepository.restoreListing(listing)
            if (result.isSuccess) {
                _actionMessage.value = "Restored: ${listing.title}"
                refreshDashboard()
            } else {
                _actionMessage.value = "Failed to restore listing"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
    fun toggleAutoApproval(enabled: Boolean) {
        viewModelScope.launch {
            _isAutoApprovalEnabled.value = enabled
            val res = adminRepository.setAutoApprovalEnabled(enabled)
            if (res.isSuccess) {
                _actionMessage.value = if (enabled) "Auto-Approval Turned ON (Listings approved instantly)" else "Auto-Approval Turned OFF (Manual review required)"
                _auditLogs.value = adminRepository.getAuditLogs(limit = 100)
            } else {
                _isAutoApprovalEnabled.value = !enabled
                _actionMessage.value = "Failed to update Auto-Approval setting"
            }
        }
    }
}
