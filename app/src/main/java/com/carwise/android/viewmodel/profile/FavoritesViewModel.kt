package com.carwise.android.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.Listing
import com.carwise.android.model.repository.CarwiseRepository
import com.carwise.android.data.model.ResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesState(
    val isLoading: Boolean = false,
    val listings: List<Listing> = emptyList(),
    val total: Int = 0,
    val error: String? = null,
    val currentPage: Int = 1,
    val isPaginating: Boolean = false,
    val endReached: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FavoritesState())
    val state: StateFlow<FavoritesState> = _state

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, currentPage = 1, endReached = false)
            try {
                val result = repository.getFavorites(page = 1, limit = 10)
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
                        _state.value = _state.value.copy(isLoading = true)
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Bir hata oluştu"
                )
            }
        }
    }

    fun loadMoreFavorites() {
        val state = _state.value
        if (state.isPaginating || state.endReached) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isPaginating = true)
            val nextPage = state.currentPage + 1
            try {
                val result = repository.getFavorites(page = nextPage, limit = 10)
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

    fun refreshFavorites() {
        loadFavorites()
    }
} 