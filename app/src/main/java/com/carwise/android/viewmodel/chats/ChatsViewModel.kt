package com.carwise.android.viewmodel.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.Chat
import com.carwise.android.data.model.GetChatResponse
import com.carwise.android.data.model.ResultState
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val isRefreshing: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {

    private val _chatsState = MutableStateFlow(ChatsState())
    val chatsState: StateFlow<ChatsState> = _chatsState.asStateFlow()

    private var currentPage = 1
    private val pageSize = 20
    private var isLoadingInitial = true

    init {
        loadChats()
    }

    fun loadChats() {
        if (isLoadingInitial && _chatsState.value.chats.isNotEmpty()) {
            isLoadingInitial = false
            return
        }

        viewModelScope.launch {
            _chatsState.update { 
                it.copy(
                    isLoading = true,
                    error = null,
                    isRefreshing = false
                )
            }

            try {
                when (val result = repository.getChats(page = 1, limit = pageSize)) {
                    is ResultState.Success -> {
                        val response = result.data
                        _chatsState.update {
                            it.copy(
                                chats = response.chats,
                                isLoading = false,
                                isRefreshing = false,
                                endReached = response.chats.size < pageSize,
                                error = null
                            )
                        }
                        isLoadingInitial = false
                        currentPage = 1
                    }
                    is ResultState.Error -> {
                        _chatsState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = result.error.error ?: "Sohbetler yüklenirken bir hata oluştu"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        if (isLoadingInitial) {
                            _chatsState.update {
                                it.copy(
                                    isLoading = true,
                                    error = null
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _chatsState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Sohbetler yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun loadOlderChats() {
        if (_chatsState.value.isPaginating || 
            _chatsState.value.endReached || 
            _chatsState.value.isLoading || 
            _chatsState.value.isRefreshing ||
            currentPage <= 1
        ) return

        viewModelScope.launch {
            _chatsState.update { 
                it.copy(
                    isPaginating = true,
                    error = null
                )
            }

            val nextPage = currentPage - 1
            if (nextPage < 1) {
                _chatsState.update {
                    it.copy(
                        isPaginating = false,
                        endReached = true
                    )
                }
                return@launch
            }

            try {
                when (val result = repository.getChats(page = nextPage, limit = pageSize)) {
                    is ResultState.Success -> {
                        val response = result.data
                        if (response.chats.isNotEmpty()) {
                            _chatsState.update {
                                it.copy(
                                    chats = response.chats + it.chats,
                                    isPaginating = false,
                                    endReached = response.chats.size < pageSize,
                                    error = null
                                )
                            }
                            currentPage = nextPage
                        } else {
                            _chatsState.update {
                                it.copy(
                                    isPaginating = false,
                                    endReached = true
                                )
                            }
                        }
                    }
                    is ResultState.Error -> {
                        _chatsState.update {
                            it.copy(
                                isPaginating = false,
                                error = result.error.error ?: "Daha eski sohbetler yüklenirken bir hata oluştu"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _chatsState.update {
                            it.copy(
                                isPaginating = true,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _chatsState.update {
                    it.copy(
                        isPaginating = false,
                        error = e.message ?: "Daha eski sohbetler yüklenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun refreshChats() {
        if (_chatsState.value.isLoading || 
            _chatsState.value.isPaginating || 
            _chatsState.value.isRefreshing
        ) return

        viewModelScope.launch {
            _chatsState.update { 
                it.copy(
                    isRefreshing = true,
                    error = null
                )
            }
            currentPage = 1

            try {
                when (val result = repository.getChats(page = currentPage, limit = pageSize)) {
                    is ResultState.Success -> {
                        val response = result.data
                        _chatsState.update {
                            it.copy(
                                chats = response.chats,
                                isRefreshing = false,
                                endReached = response.chats.size < pageSize,
                                error = null
                            )
                        }
                    }
                    is ResultState.Error -> {
                        _chatsState.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.error.error ?: "Sohbetler yenilenirken bir hata oluştu"
                            )
                        }
                    }
                    is ResultState.Loading -> {
                        _chatsState.update {
                            it.copy(
                                isRefreshing = true,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _chatsState.update {
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "Sohbetler yenilenirken bir hata oluştu"
                    )
                }
            }
        }
    }

    fun clearError() {
        _chatsState.update { it.copy(error = null) }
    }
} 