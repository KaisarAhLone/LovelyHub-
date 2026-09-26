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

    private val _selectedListing = MutableStateFlow<Listing?>(null)
    val selectedListing: StateFlow<Listing?> = _selectedListing.asStateFlow()

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

    fun selectListing(listing: Listing) {
        _selectedListing.value = listing
    }

    fun clearSelectedListing() {
        _selectedListing.value = null
    }

    fun postListing(context: Context, listing: Listing, imageUri: Uri?, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            repository.addListing(context, listing, imageUri)
                .onSuccess {
                    fetchListings(listing.category)
                    onResult(null)
                }
                .onFailure { error ->
                    onResult(error.localizedMessage ?: "Failed to add listing")
                }
        }
    }
}
