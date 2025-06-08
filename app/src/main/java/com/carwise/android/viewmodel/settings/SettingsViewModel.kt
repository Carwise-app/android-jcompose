package com.carwise.android.viewmodel.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carwise.android.util.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val notificationManager: NotificationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val appNotifications = notificationManager.isAppNotificationsEnabled()
                val emailNotifications = notificationManager.isEmailNotificationsEnabled()

                _state.update { it.copy(
                    appNotificationsEnabled = appNotifications,
                    emailNotificationsEnabled = emailNotifications,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Ayarlar yüklenirken bir hata oluştu: ${e.message}"
                ) }
            }
        }
    }

    fun showNotificationDialog() {
        _state.update { it.copy(showNotificationDialog = true) }
    }

    fun hideNotificationDialog() {
        _state.update { it.copy(
            showNotificationDialog = false,
            appNotificationsEnabled = false
        ) }
    }

    fun updateAppNotifications(enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    notificationManager.setAppNotificationsEnabled(false)
                    _state.update { it.copy(
                        appNotificationsEnabled = false,
                        isLoading = false,
                        error = null
                    ) }
                } catch (e: Exception) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Bildirim ayarları güncellenirken bir hata oluştu: ${e.message}"
                    ) }
                }
            }
            return
        }
        showNotificationDialog()
    }

    fun proceedWithNotificationPermission() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true,
                showNotificationDialog = false
            ) }
            try {
                val hasPermission = notificationManager.getDeviceToken() != null

                if (!hasPermission) {
                    notificationManager.initialize()
                    val permissionGranted = notificationManager.getDeviceToken() != null

                    if (!permissionGranted) {
                        _state.update { it.copy(
                            isLoading = false,
                            shouldOpenSystemSettings = true,
                            appNotificationsEnabled = false
                        ) }
                        return@launch
                    }
                }

                notificationManager.setAppNotificationsEnabled(true)
                _state.update { it.copy(
                    appNotificationsEnabled = true,
                    isLoading = false,
                    error = null,
                    shouldOpenSystemSettings = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Bildirim ayarları güncellenirken bir hata oluştu: ${e.message}",
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = false
                ) }
            }
        }
    }

    fun resetSystemSettingsFlag() {
        viewModelScope.launch {
            val hasPermission = notificationManager.getDeviceToken() != null
            if (hasPermission) {
                notificationManager.setAppNotificationsEnabled(true)
                _state.update { it.copy(
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = true
                ) }
            } else {
                _state.update { it.copy(
                    shouldOpenSystemSettings = false,
                    appNotificationsEnabled = false
                ) }
            }
        }
    }

    fun updateEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                notificationManager.setEmailNotificationsEnabled(enabled)
                _state.update { it.copy(
                    emailNotificationsEnabled = enabled,
                    isLoading = false,
                    error = null
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = "Email bildirim ayarları güncellenirken bir hata oluştu: ${e.message}"
                ) }
            }
        }
    }

    fun showDataUsageDialog() {
        _state.update { it.copy(showDataUsageDialog = true) }
    }

    fun hideDataUsageDialog() {
        _state.update { it.copy(showDataUsageDialog = false) }
    }

    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                context.cacheDir.deleteRecursively()
                _state.update { it.copy(isLoading = false, cacheCleared = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun openPrivacyPolicy() {
        _state.update { it.copy(shouldOpenPrivacyPolicy = true) }
    }

    fun resetPrivacyPolicyFlag() {
        _state.update { it.copy(shouldOpenPrivacyPolicy = false) }
    }

    fun openKVKK() {
        _state.update { it.copy(shouldOpenKVKK = true) }
    }

    fun resetKVKKFlag() {
        _state.update { it.copy(shouldOpenKVKK = false) }
    }

    fun openLicenses() {
        _state.update { it.copy(shouldOpenLicenses = true) }
    }

    fun resetLicensesFlag() {
        _state.update { it.copy(shouldOpenLicenses = false) }
    }

    fun openAbout() {
        _state.update { it.copy(shouldOpenAbout = true) }
    }

    fun resetAboutFlag() {
        _state.update { it.copy(shouldOpenAbout = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class SettingsState(
    val appNotificationsEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val shouldOpenSystemSettings: Boolean = false,
    val showNotificationDialog: Boolean = false,
    val showDataUsageDialog: Boolean = false,
    val shouldOpenPrivacyPolicy: Boolean = false,
    val shouldOpenKVKK: Boolean = false,
    val shouldOpenLicenses: Boolean = false,
    val shouldOpenAbout: Boolean = false,
    val cacheCleared: Boolean = false
) 