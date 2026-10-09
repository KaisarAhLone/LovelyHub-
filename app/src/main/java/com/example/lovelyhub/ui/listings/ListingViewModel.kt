package com.example.lovelyhub.ui.listings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovelyhub.data.model.Listing
import com.example.lovelyhub.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ListingState {
    object Loading : ListingState()
    data class Success(val items: List<Listing>) : ListingState()
    data class Error(val message: String) : ListingState()
}

class ListingViewModel(
    private val repository: ListingRepository = ListingRepository()
) : ViewModel() {

    private val _listingState = MutableStateFlow<ListingState>(ListingState.Loading)
    val listingState: StateFlow<ListingState> = _listingState.asStateFlow()

    private val _userListings = MutableStateFlow<List<Listing>>(emptyList())
    val userListings: StateFlow<List<Listing>> = _userListings.asStateFlow()

    fun fetchListings(category: String) {
        viewModelScope.launch {
            _listingState.value = ListingState.Loading
            try {
                val items = repository.getListings(category)
                _listingState.value = ListingState.Success(items)
            } catch (e: Exception) {
                _listingState.value = ListingState.Error(e.localizedMessage ?: "Failed to load listings")
            }
        }
    }

    suspend fun compressImageSync(context: Context, uri: Uri): String {
        return repository.compressAndEncodeImage(context, uri)
    }

    fun fetchUserListings() {
        viewModelScope.launch {
            _userListings.value = repository.getUserListings()
        }
    }

    fun postListing(context: Context, listing: Listing, imageUri: Uri?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.addListing(context, listing, imageUri)
                .onSuccess {
                    fetchListings(listing.category)
                    fetchUserListings()
                    onResult(null)
                }
                .onFailure { error ->
                    onResult(error.localizedMessage ?: "Failed to post listing")
                }
        }
    }

    fun updateListing(context: Context, listing: Listing, newImageUri: Uri?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.updateListing(context, listing, newImageUri)
                .onSuccess {
                    fetchListings(listing.category)
                    fetchUserListings()
                    onResult(null)
                }
                .onFailure { error ->
                    onResult(error.localizedMessage ?: "Failed to update listing")
                }
        }
    }

    fun deleteListing(listingId: String, category: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteListing(listingId)
            fetchListings(category)
            fetchUserListings()
            onComplete()
        }
    }

    fun toggleFavorite(listingId: String, currentFavState: Boolean, category: String) {
        viewModelScope.launch {
            repository.toggleFavorite(listingId, currentFavState)
            fetchListings(category)
        }
    }
}
