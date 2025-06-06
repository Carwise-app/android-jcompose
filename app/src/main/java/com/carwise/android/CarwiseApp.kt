package com.carwise.android

import android.app.Application
import com.carwise.android.util.NotificationManager
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class CarwiseApp : Application() {
    
    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        notificationManager.initialize()
    }
}