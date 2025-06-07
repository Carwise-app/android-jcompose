package com.carwise.android

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
        Log.d("DeepLink", "Intent action: ${intent?.action}")
        Log.d("DeepLink", "Intent data: ${intent?.data}")

        if (intent?.action == Intent.ACTION_VIEW) {
            val deepLinkUri: Uri? = intent.data
            Log.d("DeepLink", "Deep link URI: $deepLinkUri")

            deepLinkUri?.let { uri ->
                Log.d("DeepLink", "Scheme: ${uri.scheme}, Host: ${uri.host}, Path: ${uri.path}")

                when {
                    // Custom scheme: carwise://reset-password?token=...&email=...
                    uri.scheme == "carwise" && uri.host == "reset-password" -> {
                        val token = uri.getQueryParameter("token") ?: ""
                        val email = uri.getQueryParameter("email") ?: ""
                        Log.d("DeepLink", "Custom scheme - Token: $token, Email: $email")

                        if (token.isNotEmpty() && email.isNotEmpty()) {
                            startDestination = "reset_password?token=$token&email=$email"
                            Log.d("DeepLink", "StartDestination set to: $startDestination")
                        } else {

                        }
                    }

                    // HTTPS reset password: https://carwisegw.yusuftalhaklc.com/auth/reset-password?token=...&email=...
                    uri.scheme == "https" &&
                            uri.host == "carwisegw.yusuftalhaklc.com" &&
                            uri.path?.startsWith("/auth/reset-password") == true -> {
                        val token = uri.getQueryParameter("token") ?: ""
                        val email = uri.getQueryParameter("email") ?: ""
                        Log.d("DeepLink", "HTTPS reset password - Token: $token, Email: $email")

                        if (token.isNotEmpty() && email.isNotEmpty()) {
                            startDestination = "reset_password?token=$token&email=$email"
                            Log.d("DeepLink", "StartDestination set to: $startDestination")
                        } else {

                        }
                    }

                    // Listing detail: https://carwisegw.yusuftalhaklc.com/listing/fiat-egea-sorunsuz-dd71dd76
                    uri.scheme == "https" &&
                            uri.host == "carwisegw.yusuftalhaklc.com" &&
                            uri.path?.startsWith("/listing/") == true -> {
                        val listingId = uri.pathSegments.lastOrNull() ?: ""
                        Log.d("DeepLink", "Listing detail - ID: $listingId")

                        if (listingId.isNotEmpty()) {
                            startDestination = "listing_detail/$listingId"
                            Log.d("DeepLink", "StartDestination set to: $startDestination")
                        } else {

                        }
                    }

                    else -> {
                        Log.d("DeepLink", "Unknown deep link format")
                    }
                }
            }
        } else {
            Log.d("DeepLink", "Not a VIEW intent")
        }
    }



    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
        recreate()
    }
}

