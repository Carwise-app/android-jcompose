package com.carwise.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.carwise.android.view.splash.SplashScreen
import com.carwise.android.view.auth.LoginScreen
import com.carwise.android.view.auth.RegisterScreen
import com.carwise.android.view.auth.ForgotPasswordScreen
import com.carwise.android.view.auth.ResetPasswordScreen
import com.carwise.android.view.home.HomeScreen
import com.carwise.android.view.listing.ListingDetailScreen
import com.carwise.android.view.create_listing.CreateListingScreen
import com.carwise.android.view.listing.MyListingsScreen
import com.carwise.android.view.profile.ProfileScreen
import com.carwise.android.view.settings.SettingsScreen
import com.carwise.android.view.settings.PrivacyPolicyScreen
import com.carwise.android.view.settings.KVKKScreen
import com.carwise.android.view.settings.LicensesScreen
import com.carwise.android.view.settings.AboutScreen
import com.carwise.android.view.upload.UploadPredictScreen
import com.carwise.android.view.price_prediction.PricePredictionScreen
import com.carwise.android.view.price_prediction.PricePredictionHistoryScreen
import com.carwise.android.view.chats.ChatsScreen
import com.carwise.android.view.messages.MessagesScreen
import com.carwise.android.view.update_listing.UpdateListingScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password?token={token}&email={email}")
    object Home : Screen("home")
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    object CreateListing : Screen("create_listing")
    object UpdateListing : Screen("update_listing/{listingId}") {
        fun createRoute(listingId: String) = "update_listing/$listingId"
    }
    object ListingList : Screen("listing")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
    object MyListings : Screen("my_listings")
    object Favorites : Screen("favorites")
    object Listings : Screen("listings")
    object UploadPredict : Screen("upload_predict")
    object PricePrediction : Screen("price_prediction")
    object PricePredictionHistory : Screen("price_prediction_history")
    object Chats : Screen("chats")
    object Messages : Screen("messages/{listingId}/{userId}") {
        fun createRoute(listingId: String, userId: String) = "messages/$listingId/$userId"
    }
    object PrivacyPolicy : Screen("settings/privacy_policy")
    object KVKK : Screen("settings/kvkk")
    object Licenses : Screen("settings/licenses")
    object About : Screen("settings/about")
}

@Composable
fun CarwiseNavigation (
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }   
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }
        composable(
            route = Screen.ResetPassword.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "carwise://reset-password?token={token}&email={email}"
                }
            ),
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                },
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(navController = navController, token = token, email = email)
        }   
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://carwisegw.yusuftalhaklc.com/listing/{listingId}"
                },
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId")
            ListingDetailScreen(navController, listingId)
        }
        composable(Screen.CreateListing.route) {
            CreateListingScreen(navController)
        }
        composable(
            route = Screen.UpdateListing.route,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId")
            if (listingId != null) {
                UpdateListingScreen(navController = navController, listingId = listingId)
            }
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }   
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController)
        }
        composable(Screen.KVKK.route) {
            KVKKScreen(navController)
        }
        composable(Screen.Licenses.route) {
            LicensesScreen(navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController)
        }
        composable(route = Screen.MyListings.route) {
            com.carwise.android.view.listing.MyListingsScreen(navController)
        }
        composable(route = Screen.Favorites.route) {
            com.carwise.android.view.profile.FavoritesScreen(navController)
        }
        composable(Screen.Listings.route) {
            com.carwise.android.view.listing.ListingsScreen(navController)
        }
        composable(Screen.UploadPredict.route) {
            UploadPredictScreen(navController)
        }
        composable(Screen.PricePrediction.route) {
            PricePredictionScreen(navController)
        }
        composable(Screen.PricePredictionHistory.route) {
            PricePredictionHistoryScreen(navController)
        }
        composable(Screen.Chats.route) {
            ChatsScreen(navController)
        }
        composable(
            route = Screen.Messages.route,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                },
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MessagesScreen(navController, listingId, userId)
        }
    }
}