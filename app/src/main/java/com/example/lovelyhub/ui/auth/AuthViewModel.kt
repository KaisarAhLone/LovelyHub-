package com.example.lovelyhub.ui.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovelyhub.data.local.LocalUserCache
import com.example.lovelyhub.data.model.User
import com.example.lovelyhub.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    val currentUser: FirebaseUser? get() = repository.currentUser

    init {
        repository.currentUser?.let { user ->
            _authState.value = AuthState.Success(user)
            loadUserProfile(user.uid)
        }
    }

    fun loadUserProfile(uid: String, context: Context? = null) {
        viewModelScope.launch {
            val userCache = context?.let { LocalUserCache(it) }
            val cachedPhoto = userCache?.getProfilePhoto(uid)

            if (!cachedPhoto.isNullOrBlank()) {
                val current = _currentUserProfile.value
                if (current == null) {
                    _currentUserProfile.value = User(uid = uid, photoUrl = cachedPhoto)
                } else if (current.photoUrl.isBlank()) {
                    _currentUserProfile.value = current.copy(photoUrl = cachedPhoto)
                }
            }

            val networkProfile = repository.fetchUserProfile(uid)
            if (networkProfile != null) {
                val finalPhoto = networkProfile.photoUrl.ifBlank { cachedPhoto ?: "" }
                val updatedProfile = networkProfile.copy(photoUrl = finalPhoto)
                _currentUserProfile.value = updatedProfile
                if (finalPhoto.isNotBlank()) {
                    userCache?.saveProfilePhotoBase64(uid, finalPhoto)
                }
            } else if (cachedPhoto != null && _currentUserProfile.value == null) {
                _currentUserProfile.value = User(uid = uid, photoUrl = cachedPhoto)
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithEmail(email.trim(), password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    loadUserProfile(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Login failed")
                }
        }
    }

    fun signUpWithEmail(
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
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all required fields")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signUpWithEmail(
                context = context,
                name = name.trim(),
                email = email.trim(),
                password = password,
                role = role,
                businessName = businessName.trim(),
                businessPhone = businessPhone.trim(),
                businessWhatsApp = businessWhatsApp.trim(),
                businessLocation = businessLocation.trim(),
                openTime = openTime,
                closeTime = closeTime,
                is24Hours = is24Hours,
                imageUri = imageUri
            )
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    loadUserProfile(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Registration failed")
                }
        }
    }

    fun signInWithGoogle(
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
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithGoogle(
                context = context,
                webClientId = webClientId,
                isRegistration = isRegistration,
                name = name,
                role = role,
                businessName = businessName,
                businessPhone = businessPhone,
                businessWhatsApp = businessWhatsApp,
                businessLocation = businessLocation,
                openTime = openTime,
                closeTime = closeTime,
                is24Hours = is24Hours,
                imageUri = imageUri
            )
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    loadUserProfile(user.uid, context)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Google Sign-In failed")
                }
        }
    }

    fun updateUserProfile(
        context: Context? = null,
        name: String,
        email: String,
        role: String,
        businessName: String = "",
        businessPhone: String = "",
        businessLocation: String = "",
        photoUrl: String,
        openTime: String = "09:00 AM",
        closeTime: String = "10:00 PM",
        is24Hours: Boolean = false,
        onResult: (String?) -> Unit
    ) {
        val uid = repository.currentUser?.uid ?: return onResult("User not logged in")
        viewModelScope.launch {
            val userCache = context?.let { LocalUserCache(it) }
            if (photoUrl.isNotBlank()) {
                userCache?.saveProfilePhotoBase64(uid, photoUrl)
            } else {
                userCache?.removeProfilePhoto(uid)
            }

            repository.updateUserProfile(
                uid = uid,
                name = name.trim(),
                email = email.trim(),
                role = role,
                businessName = businessName.trim(),
                businessPhone = businessPhone.trim(),
                businessLocation = businessLocation.trim(),
                photoUrl = photoUrl,
                openTime = openTime,
                closeTime = closeTime,
                is24Hours = is24Hours
            )
                .onSuccess {
                    loadUserProfile(uid, context)
                    onResult(null)
                }
                .onFailure { error ->
                    onResult(error.localizedMessage ?: "Failed to update profile")
                }
        }
    }

    fun resetPassword(email: String, onComplete: (String?) -> Unit) {
        if (email.isBlank()) {
            onComplete("Please enter your email address")
            return
        }

        viewModelScope.launch {
            repository.resetPassword(email.trim())
                .onSuccess { onComplete(null) }
                .onFailure { error -> onComplete(error.localizedMessage ?: "Failed to send reset email") }
        }
    }

    fun deleteUserAccount(context: Context, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.deleteUserAccount(context)
                .onSuccess {
                    _currentUserProfile.value = null
                    _authState.value = AuthState.Idle
                    onResult(null)
                }
                .onFailure { error ->
                    onResult(error.localizedMessage ?: "Failed to delete account")
                }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }

    fun signOut() {
        repository.signOut()
        _currentUserProfile.value = null
        _authState.value = AuthState.Idle
    }
}
