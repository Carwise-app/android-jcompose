package com.carwise.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.carwise.android.navigation.CarwiseNavigation
import com.carwise.android.navigation.Screen
import com.carwise.android.ui.theme.CarwiseTheme
import dagger.hilt.android.AndroidEntryPoint
import com.onesignal.OneSignal


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var startDestination = Screen.Splash.route

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        enableEdgeToEdge()
        handleDeepLink(intent)

        setContent {
            CarwiseTheme {
                val navController = rememberNavController()
                CarwiseNavigation(
                    navController = navController,
                    startDestination = startDestination
                )
            }
        }
    }

    private fun handleDeepLink(intent: Intent?) {

        if (intent?.action == Intent.ACTION_VIEW) {
            val deepLinkUri: Uri? = intent.data

            deepLinkUri?.let { uri ->

                when {
                    uri.scheme == "carwise" && uri.host == "reset-password" -> {
                        val token = uri.getQueryParameter("token") ?: ""
                        val email = uri.getQueryParameter("email") ?: ""


                        if (token.isNotEmpty() && email.isNotEmpty()) {
                            startDestination = "${Screen.ResetPassword.route}?token=$token&email=$email"
                        } else {
                        }
                    }
                    else -> {
                    }
                }
            }
        } else {
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
        recreate()
    }
}

