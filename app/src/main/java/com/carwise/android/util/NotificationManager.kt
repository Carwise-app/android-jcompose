package com.carwise.android.util

import android.content.Context
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.SetGetNotificationRequest
import com.carwise.android.model.repository.CarwiseRepository
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CarwiseRepository
) {
    val oneSignalAppId = "a8698bbe-abec-4116-9b41-75acdd3c1139"
    fun initialize() {
        try {
            // OneSignal'ı debug modunda başlat
            OneSignal.Debug.logLevel = LogLevel.VERBOSE

            // OneSignal'ı başlat
            OneSignal.initWithContext(context, oneSignalAppId)

            // Kullanıcı izinlerini iste (coroutine içinde)
            CoroutineScope(Dispatchers.Main).launch {
                // Önce mevcut izin durumunu kontrol et
                val currentPermission = OneSignal.Notifications.permission
                
                // Eğer izin verilmemişse, izin dialogunu göster
                if (!currentPermission) {
                    OneSignal.Notifications.requestPermission(true)
                }
                
                // İzin alındıktan sonra token'ı backend'e gönder
                syncNotificationSettings()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun syncNotificationSettings() {
        try {
            val deviceToken = getDeviceToken()
            if (deviceToken != null) {
                val currentSettings = repository.getNotify()
                if (currentSettings is ResultState.Success) {
                    val settings = currentSettings.data
                    repository.setNotify(
                        deviceToken = deviceToken,
                        emailNotify = settings.emailNotify,
                        pushNotify = settings.pushNotify
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setAppNotificationsEnabled(enabled: Boolean) {
        try {
            if (enabled) {
                // Önce sistem bildirim iznini kontrol et
                val hasPermission = OneSignal.Notifications.permission
                
                if (!hasPermission) {
                    // Bildirim izni yoksa, izin dialogunu göster
                    CoroutineScope(Dispatchers.Main).launch {
                        val permissionGranted = OneSignal.Notifications.requestPermission(true)
                        
                        // İzin verilmediyse backend'e false gönder ve işlemi sonlandır
                        if (!permissionGranted) {
                            val deviceToken = getDeviceToken()
                            if (deviceToken != null) {
                                val currentSettings = repository.getNotify()
                                if (currentSettings is ResultState.Success) {
                                    val settings = currentSettings.data
                                    repository.setNotify(
                                        deviceToken = deviceToken,
                                        emailNotify = settings.emailNotify,
                                        pushNotify = false
                                    )
                                }
                            }
                            // Hata fırlatmak yerine sadece return yap
                            return@launch
                        }
                    }
                }
            } else {
                // Bildirimleri devre dışı bırak
                OneSignal.Notifications.clearAllNotifications()
            }
            
            // Backend'e bildirim ayarlarını gönder
            val deviceToken = getDeviceToken()
            if (deviceToken != null) {
                val currentSettings = repository.getNotify()
                if (currentSettings is ResultState.Success) {
                    val settings = currentSettings.data
                    // Sistem izni ve istenen durum aynı ise backend'e gönder
                    val systemPermission = OneSignal.Notifications.permission
                    repository.setNotify(
                        deviceToken = deviceToken,
                        emailNotify = settings.emailNotify,
                        pushNotify = enabled && systemPermission
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Hata fırlatmak yerine loglama yap
            Log.e("NotificationManager", "Bildirim ayarları güncellenirken bir hata oluştu: ${e.message}")
        }
    }

    suspend fun setEmailNotificationsEnabled(enabled: Boolean) {
        try {
            // Backend'e email bildirim ayarlarını gönder
            val deviceToken = getDeviceToken()
            if (deviceToken != null) {
                val currentSettings = repository.getNotify()
                if (currentSettings is ResultState.Success) {
                    val settings = currentSettings.data
                    repository.setNotify(
                        deviceToken = deviceToken,
                        emailNotify = enabled,
                        pushNotify = settings.pushNotify
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw NotificationException("Email bildirim ayarları güncellenirken bir hata oluştu: ${e.message}")
        }
    }

    suspend fun isAppNotificationsEnabled(): Boolean {
        return try {
            // Hem sistem iznini hem de backend'deki durumu kontrol et
            val systemPermission = OneSignal.Notifications.permission
            val currentSettings = repository.getNotify()
            
            if (currentSettings is ResultState.Success) {
                // Her ikisi de true ise true döndür
                currentSettings.data.pushNotify && systemPermission
            } else {
                // Backend'den veri alınamadıysa sistem iznine bak
                systemPermission
            }
        } catch (e: Exception) {
            e.printStackTrace()
            OneSignal.Notifications.permission
        }
    }

    suspend fun isEmailNotificationsEnabled(): Boolean {
        return try {
            val currentSettings = repository.getNotify()
            if (currentSettings is ResultState.Success) {
                currentSettings.data.emailNotify
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDeviceToken(): String? {
        return try {
            OneSignal.User.pushSubscription.token
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class NotificationException(message: String) : Exception(message)