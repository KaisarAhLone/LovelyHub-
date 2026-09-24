package com.example.lovelyhub.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadUserProfile(uid: String) {
        viewModelScope.launch {
            _currentUserProfile.value = repository.fetchUserProfile(uid)
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

    fun signUpWithEmail(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signUpWithEmail(name.trim(), email.trim(), password)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    loadUserProfile(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Registration failed")
                }
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.signInWithGoogle(context, webClientId)
                .onSuccess { user ->
                    _authState.value = AuthState.Success(user)
                    loadUserProfile(user.uid)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.localizedMessage ?: "Google Sign-In failed")
                }
        }
    }

    fun updateUserProfile(
        name: String,
        email: String,
        role: String,
        photoUrl: String,
        onResult: (String?) -> Unit
    ) {
        val uid = repository.currentUser?.uid ?: return onResult("User not logged in")
        viewModelScope.launch {
            repository.updateUserProfile(uid, name.trim(), email.trim(), role, photoUrl)
                .onSuccess {
                    loadUserProfile(uid)
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
