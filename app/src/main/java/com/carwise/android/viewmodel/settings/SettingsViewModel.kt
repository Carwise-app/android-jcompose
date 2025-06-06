package com.carwise.android.viewmodel.settings

import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.data.model.ResultState
import com.carwise.android.util.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val appNotificationsEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldOpenSystemSettings: Boolean = false,
    val showNotificationDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            try {
                val appNotifications = notificationManager.isAppNotificationsEnabled()
                val emailNotifications = notificationManager.isEmailNotificationsEnabled()
                
                _state.value = _state.value.copy(
                    appNotificationsEnabled = appNotifications,
                    emailNotificationsEnabled = emailNotifications,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Ayarlar yüklenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun showNotificationDialog() {
        _state.value = _state.value.copy(showNotificationDialog = true)
    }

    fun hideNotificationDialog() {
        _state.value = _state.value.copy(
            showNotificationDialog = false,
            appNotificationsEnabled = false
        )
    }

    fun updateAppNotifications(enabled: Boolean) {
        if (!enabled) {
            // Bildirimleri kapatma işlemi
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)
                try {
                    notificationManager.setAppNotificationsEnabled(false)
                    _state.value = _state.value.copy(
                        appNotificationsEnabled = false,
                        isLoading = false,
                        error = null
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Bildirim ayarları güncellenirken bir hata oluştu: ${e.message}"
                    )
                }
            }
            return
        }

        // Bildirimleri açma işlemi için önce dialog göster
        showNotificationDialog()
    }

    fun proceedWithNotificationPermission() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                showNotificationDialog = false
            )
            try {
                // Önce sistem iznini kontrol et
                val hasPermission = notificationManager.getDeviceToken() != null
                
                if (!hasPermission) {
                    // İzin yoksa, sistem izin dialogunu göster
                    notificationManager.initialize()
                    
                    // İzin durumunu tekrar kontrol et
                    val permissionGranted = notificationManager.getDeviceToken() != null
                    
                    if (!permissionGranted) {
                        // İzin hala verilmediyse sistem ayarlarına yönlendir
                        _state.value = _state.value.copy(
                            isLoading = false,
                            shouldOpenSystemSettings = true,
                            appNotificationsEnabled = false
                        )
                        return@launch
                    }
                }
                
                // İzin varsa bildirimleri aç
                notificationManager.setAppNotificationsEnabled(true)
                _state.value = _state.value.copy(
                    appNotificationsEnabled = true,
                    isLoading = false,
                    error = null,
                    shouldOpenSystemSettings = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Bildirim ayarları güncellenirken bir hata oluştu: ${e.message}",
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = false
                )
            }
        }
    }

    fun resetSystemSettingsFlag() {
        viewModelScope.launch {
            // Sistem ayarlarından dönüldüğünde bildirim durumunu kontrol et
            val hasPermission = notificationManager.getDeviceToken() != null
            if (hasPermission) {
                // İzin verilmişse bildirimleri aç
                notificationManager.setAppNotificationsEnabled(true)
                _state.value = _state.value.copy(
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = true
                )
            } else {
                // İzin hala verilmemişse switch'i false yap
                _state.value = _state.value.copy(
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = false
                )
            }
        }
    }

    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                notificationManager.setEmailNotificationsEnabled(enabled)
                _state.value = _state.value.copy(
                    emailNotificationsEnabled = enabled,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Email bildirim ayarları güncellenirken bir hata oluştu: ${e.message}"
                )
            }
        }
    }
} 