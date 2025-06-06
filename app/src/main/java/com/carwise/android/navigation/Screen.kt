sealed class Screen(val route: String) {
    object Listings : Screen("listings")
    object UploadPredict : Screen("upload_predict")
    data object PricePrediction : Screen("price_prediction")
    object NotificationSettings : Screen("notification_settings")

    companion object {
        fun createRoute(route: String, vararg args: String): String {
            return buildString {
                append(route)
                args.forEach { arg ->
                    append("/$arg")
                }
            }
        }
    }
} 