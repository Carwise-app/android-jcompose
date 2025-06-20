package com.carwise.android.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.ListListingResponse
import com.carwise.android.data.model.Listing
import com.carwise.android.data.model.Notification
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UserPayload
import com.carwise.android.model.repository.CarwiseRepository
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = false,
    val user: UserPayload? = null,
    val error: String? = null,
    val isListingsLoading: Boolean = false,
    val listings: List<Listing> = emptyList(),
    val total: Int = 0,
    val currentPage: Int = 1,
    val isPaginating: Boolean = false,
    val endReached: Boolean = false,
    val lastUpdated: Long = 0L,
    //----------------------
    val isNotificationsLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val notificationTotal: Long = 0L,
    val unreadCount: Long = 0L,
    val notificationCurrentPage: Int = 1,
    val isNotificationPaginating: Boolean = false,
    val notificationEndReached: Boolean = false,
    val notificationError: String? = null,
    val notificationLastUpdated: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CarwiseRepository
) : ViewModel() {
    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState

    private var cachedListings: List<Listing> = emptyList()
    private var lastRefreshTime: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000 // 5 minutes cache

    // Notification cache
    private var cachedNotifications: List<Notification> = emptyList()
    private var lastNotificationRefreshTime: Long = 0
    private val NOTIFICATION_CACHE_DURATION = 2 * 60 * 1000 // 2 minutes cache for notifications

    init {
        loadUserInfo()
        loadListings()
        loadNotifications()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            if (_homeState.value.user != null) return@launch // Skip if we already have user data
            
            _homeState.value = _homeState.value.copy(isLoading = true)
            try {
                val currentUser = repository.getCurrentUser()
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    user = currentUser,
                    lastUpdated = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadListings() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < CACHE_DURATION && cachedListings.isNotEmpty()) {
            _homeState.value = _homeState.value.copy(
                listings = cachedListings,
                lastUpdated = currentTime
            )
            return
        }

        viewModelScope.launch {
            if (_homeState.value.isListingsLoading) return@launch // Prevent multiple simultaneous loads
            
            _homeState.value = _homeState.value.copy(isListingsLoading = true, currentPage = 1, endReached = false)
            try {
                val result = repository.listListing(
                    page = 1,
                    limit = 20, // Increased page size for better performance
                    sortBy = "created_at",
                    sortOrder = "desc"
                )
                when (result) {
                    is ResultState.Success -> {
                        cachedListings = result.data.listings
                        lastRefreshTime = currentTime
                        _homeState.value = _homeState.value.copy(
                            isListingsLoading = false,
                            listings = cachedListings,
                            total = result.data.total,
                            currentPage = 1,
                            endReached = result.data.listings.size >= result.data.total,
                            lastUpdated = currentTime
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            isListingsLoading = false,
                            error = result.error.error
                        )
                    }   
                    is ResultState.Loading -> {
                        _homeState.value = _homeState.value.copy(
                            isListingsLoading = true
                        )
                    }
                }       
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isListingsLoading = false,
                    error = "İlanlar yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun loadMoreListings() {
        val state = _homeState.value
        if (state.isPaginating || state.endReached || state.isListingsLoading) return
        
        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isPaginating = true)
            val nextPage = state.currentPage + 1
            try {
                val result = repository.listListing(
                    page = nextPage,
                    limit = 20, // Increased page size
                    sortBy = "created_at",
                    sortOrder = "desc"
                )
                when (result) {
                    is ResultState.Success -> {
                        val newList = (state.listings + result.data.listings).distinctBy { it.id }
                        cachedListings = newList
                        lastRefreshTime = System.currentTimeMillis()
                        _homeState.value = _homeState.value.copy(
                            listings = newList,
                            currentPage = nextPage,
                            isPaginating = false,
                            endReached = newList.size >= state.total,
                            lastUpdated = System.currentTimeMillis()
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(isPaginating = false)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(isPaginating = false)
            }
        }
    }

    fun refreshListings() {
        lastRefreshTime = 0 // Force refresh by resetting cache time
        loadUserInfo()
        loadListings()
        refreshNotifications()
    }

    // NOTIFICATION FUNCTIONS
    fun loadNotifications() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNotificationRefreshTime < NOTIFICATION_CACHE_DURATION && cachedNotifications.isNotEmpty()) {
            _homeState.value = _homeState.value.copy(
                notifications = cachedNotifications,
                notificationLastUpdated = currentTime
            )
            return
        }

        viewModelScope.launch {
            if (_homeState.value.isNotificationsLoading) return@launch

            _homeState.value = _homeState.value.copy(
                isNotificationsLoading = true,
                notificationCurrentPage = 1,
                notificationEndReached = false,
                notificationError = null
            )

            try {
                val result = repository.getNotifications(page = 1, limit = 20)
                when (result) {
                    is ResultState.Success -> {
                        cachedNotifications = result.data.notifications
                        lastNotificationRefreshTime = currentTime
                        _homeState.value = _homeState.value.copy(
                            isNotificationsLoading = false,
                            notifications = cachedNotifications,
                            notificationTotal = result.data.total,
                            unreadCount = result.data.unread,
                            notificationCurrentPage = 1,
                            notificationEndReached = result.data.notifications.size >= result.data.total,
                            notificationLastUpdated = currentTime
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            isNotificationsLoading = false,
                            notificationError = result.error.error
                        )
                    }
                    is ResultState.Loading -> {
                        _homeState.value = _homeState.value.copy(
                            isNotificationsLoading = true
                        )
                    }
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isNotificationsLoading = false,
                    notificationError = "Bildirimler yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun loadMoreNotifications() {
        val state = _homeState.value
        if (state.isNotificationPaginating || state.notificationEndReached || state.isNotificationsLoading) return

        viewModelScope.launch {
            _homeState.value = _homeState.value.copy(isNotificationPaginating = true)
            val nextPage = state.notificationCurrentPage + 1

            try {
                val result = repository.getNotifications(page = nextPage, limit = 20)
                when (result) {
                    is ResultState.Success -> {
                        val newNotifications = (state.notifications + result.data.notifications)
                            .distinctBy { it.id }
                        cachedNotifications = newNotifications
                        lastNotificationRefreshTime = System.currentTimeMillis()

                        _homeState.value = _homeState.value.copy(
                            notifications = newNotifications,
                            notificationTotal = result.data.total,
                            unreadCount = result.data.unread,
                            notificationCurrentPage = nextPage,
                            isNotificationPaginating = false,
                            notificationEndReached = newNotifications.size >= result.data.total,
                            notificationLastUpdated = System.currentTimeMillis()
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            isNotificationPaginating = false,
                            notificationError = result.error.error
                        )
                    }
                    else -> {
                        _homeState.value = _homeState.value.copy(isNotificationPaginating = false)
                    }
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isNotificationPaginating = false,
                    notificationError = "Daha fazla bildirim yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = repository.readNotification(notificationId)
                when (result) {
                    is ResultState.Success -> {
                        // Update local state
                        val updatedNotifications = _homeState.value.notifications.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(read = true)
                            } else {
                                notification
                            }
                        }

                        // Update cached notifications
                        cachedNotifications = updatedNotifications

                        // Update unread count
                        val newUnreadCount = maxOf(0, _homeState.value.unreadCount - 1)

                        _homeState.value = _homeState.value.copy(
                            notifications = updatedNotifications,
                            unreadCount = newUnreadCount
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            notificationError = "Bildirim okundu olarak işaretlenirken hata oluştu: ${result.error.error}"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    notificationError = "Bildirim güncellenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val result = repository.deleteNotification(notificationId)
                when (result) {
                    is ResultState.Success -> {
                        val notificationToDelete = _homeState.value.notifications.find { it.id == notificationId }
                        val updatedNotifications = _homeState.value.notifications.filter { it.id != notificationId }

                        // Update cached notifications
                        cachedNotifications = updatedNotifications

                        // Update counts
                        val newTotal = maxOf(0, _homeState.value.notificationTotal - 1)
                        val newUnreadCount = if (notificationToDelete?.read == false) {
                            maxOf(0, _homeState.value.unreadCount - 1)
                        } else {
                            _homeState.value.unreadCount
                        }

                        _homeState.value = _homeState.value.copy(
                            notifications = updatedNotifications,
                            notificationTotal = newTotal,
                            unreadCount = newUnreadCount
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            notificationError = "Bildirim silinirken hata oluştu: ${result.error.error}"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    notificationError = "Bildirim silinirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun refreshNotifications() {
        lastNotificationRefreshTime = 0 // Force refresh by resetting cache time
        loadNotifications()
    }

    fun clearNotificationError() {
        _homeState.value = _homeState.value.copy(notificationError = null)
    }

    fun readAllNotifications() {
        viewModelScope.launch {
            try {
                val result = repository.readAllNotification()
                when (result) {
                    is ResultState.Success -> {
                        // Update all notifications to read status
                        val updatedNotifications = _homeState.value.notifications.map { notification ->
                            notification.copy(read = true)
                        }
                        
                        // Update cached notifications
                        cachedNotifications = updatedNotifications
                        
                        // Reset unread count to 0
                        _homeState.value = _homeState.value.copy(
                            notifications = updatedNotifications,
                            unreadCount = 0L
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            notificationError = "Tüm bildirimler okundu olarak işaretlenirken hata oluştu: ${result.error.error}"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    notificationError = "Tüm bildirimler güncellenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun deleteAllNotifications() {
        viewModelScope.launch {
            try {
                val result = repository.deleteAllNotification()
                when (result) {
                    is ResultState.Success -> {
                        // Clear all notifications
                        cachedNotifications = emptyList()
                        lastNotificationRefreshTime = 0 // Force refresh on next load
                        
                        _homeState.value = _homeState.value.copy(
                            notifications = emptyList(),
                            notificationTotal = 0L,
                            unreadCount = 0L,
                            notificationCurrentPage = 1,
                            notificationEndReached = true
                        )
                    }
                    is ResultState.Error -> {
                        _homeState.value = _homeState.value.copy(
                            notificationError = "Tüm bildirimler silinirken hata oluştu: ${result.error.error}"
                        )
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    notificationError = "Tüm bildirimler silinirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

} 