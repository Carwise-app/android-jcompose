package com.carwise.android.viewmodel.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UpdateListingStatusRequest
import com.carwise.android.data.model.UserPayload
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingDetailState(
    val isLoading: Boolean = false,
    val listing: GetListingResponse? = null,
    val error: String? = null,
    val isFavoriteLoading: Boolean = false,
    val currentUser: UserPayload? = null,
    val isActionLoading: Boolean = false
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ListingDetailState())
    val state: StateFlow<ListingDetailState> = _state

    init {
        getCurrentUser()
    }

    fun loadListingDetail(listingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val result = repository.getListingDetail(listingId)
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            listing = result.data
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "İlan detayı yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun toggleFavorite() {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isFavoriteLoading = true)
            try {
                val result = if (_state.value.listing!!.isFavorite) {
                    repository.deleteFavorite(listingId)
                } else {
                    repository.createFavorite(listingId)
                }
                
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            listing = _state.value.listing?.copy(isFavorite = !_state.value.listing!!.isFavorite),
                            isFavoriteLoading = false
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isFavoriteLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isFavoriteLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isFavoriteLoading = false,
                    error = "Favori işlemi sırasında bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun getCurrentUser(){
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                _state.value = _state.value.copy(
                    currentUser = currentUser
                )
            }
        }
    }

    fun updateListingStatus(status: Int) {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            try {
                val result = repository.updateStatusListing(
                    listingId = listingId,
                    request = UpdateListingStatusRequest(status = status)
                )
                
                when (result) {
                    is ResultState.Success -> {
                        loadListingDetail(listingId)
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isActionLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isActionLoading = false,
                    error = "İlan durumu güncellenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun deleteListing() {
        val listingId = _state.value.listing?.id ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isActionLoading = true)
            try {
                val result = repository.deleteListing(listingId)
                
                when (result) {
                    is ResultState.Success -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            listing = null,
                            error = null
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(
                            isActionLoading = false,
                            error = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _state.value = _state.value.copy(isActionLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isActionLoading = false,
                    error = "İlan silinirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
} 