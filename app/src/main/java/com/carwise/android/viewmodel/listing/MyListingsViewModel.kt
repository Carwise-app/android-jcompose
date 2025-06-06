package com.carwise.android.viewmodel.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.ListListingResponse
import com.carwise.android.data.model.Listing
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UserPayload
import com.carwise.android.model.repository.CarwiseRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyListingsState(
    val isLoading: Boolean = false,
    val listings: List<Listing> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val user: UserPayload? = null,
    val currentPage: Int = 1,
    val isPaginating: Boolean = false,
    val endReached: Boolean = false
)

@HiltViewModel
class MyListingsViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyListingsState())
    val state: StateFlow<MyListingsState> = _state.asStateFlow()

    init {
        loadUserInfo()
        loadMyListings()
    }

    fun loadMyListings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, currentPage = 1, endReached = false)
            try {
                val currentUser = repository.getCurrentUser()
                currentUser?.let { user ->
                    val result = repository.listListing(
                        createdBy = user.user_id,
                        page = 1,
                        limit = 10,
                        sortBy = "created_at",
                        sortOrder = "desc"
                    )
                    when (result) {
                        is ResultState.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                listings = result.data.listings,
                                total = result.data.total,
                                currentPage = 1,
                                endReached = result.data.listings.size >= result.data.total
                            )
                        }
                        is ResultState.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.error.error
                            )
                        }
                        is ResultState.Loading -> {
                            _state.value = _state.value.copy(
                                isLoading = true
                            )
                        }
                    }
                } ?: run {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Kullanıcı bilgisi bulunamadı"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Bir hata oluştu"
                )
            }
        }
    }

    fun loadMoreListings() {
        val state = _state.value
        if (state.isPaginating || state.endReached || state.user == null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isPaginating = true)
            val nextPage = state.currentPage + 1
            try {
                val result = repository.listListing(
                    createdBy = state.user.user_id,
                    page = nextPage,
                    limit = 10,
                    sortBy = "created_at",
                    sortOrder = "desc"
                )
                when (result) {
                    is ResultState.Success -> {
                        val newList = (state.listings + result.data.listings).distinctBy { it.id }
                        _state.value = _state.value.copy(
                            listings = newList,
                            currentPage = nextPage,
                            isPaginating = false,
                            endReached = newList.size >= state.total
                        )
                    }
                    is ResultState.Error -> {
                        _state.value = _state.value.copy(isPaginating = false)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isPaginating = false)
            }
        }
    }

    fun refreshListings() {
        loadMyListings()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val currentUser = repository.getCurrentUser()

                if (currentUser != null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = currentUser
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Kullanıcı bilgileri alınamadı."
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
} 