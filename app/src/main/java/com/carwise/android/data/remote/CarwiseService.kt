package com.carwise.android.data.remote

import com.carwise.android.data.model.CreateBrandRequest
import com.carwise.android.data.model.CreateListingRequest
import com.carwise.android.data.model.ForgotPasswordRequest
import com.carwise.android.data.model.GetBrandsResponse
import com.carwise.android.data.model.GetChatResponse
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.data.model.GetMessageResponse
import com.carwise.android.data.model.GetNotificationResponse
import com.carwise.android.data.model.GetPricePredictHistoryResponse
import com.carwise.android.data.model.GetStatsResponse
import com.carwise.android.data.model.GetUsersResponse
import com.carwise.android.data.model.IdResponse
import com.carwise.android.data.model.ImageResponse
import com.carwise.android.data.model.ListListingResponse
import com.carwise.android.data.model.LoginRequest
import com.carwise.android.data.model.LoginResponse
import com.carwise.android.data.model.NameRequest
import com.carwise.android.data.model.PredictionResponse
import com.carwise.android.data.model.PricePredictionRequest
import com.carwise.android.data.model.PricePredictionResponse
import com.carwise.android.data.model.ProfileResponse
import com.carwise.android.data.model.RegisterRequest
import com.carwise.android.data.model.ResetPasswordRequest
import com.carwise.android.data.model.SendMessageRequest
import com.carwise.android.data.model.SetGetNotificationRequest
import com.carwise.android.data.model.UpdateListingStatusRequest
import com.carwise.android.data.model.UpdateProfileRequest
import com.carwise.android.data.model.UserInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CarwiseService {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("auth/google/id-token")
    suspend fun loginWithGoogle(
        @Query("idToken") idToken: String
    ): Response<LoginResponse>


    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/reset-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<Unit>

    @PUT("auth/reset-password")
    suspend fun resetPasword(
        @Query("token") token: String,
        @Query("email") email: String,
        @Body request: ResetPasswordRequest
    ): Response<Unit>

    /**********************************************************************************************************************/

    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @PUT("profile/edit")
    suspend fun updateProfile(
        @Part("first_name") firstName: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("country_code") countryCode: RequestBody,
        @Part("phone_number") phoneNumber: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Response<Unit>

    @PATCH("profile/notify")
    suspend fun setNotify(
        @Body request: SetGetNotificationRequest
    ): Response<Unit>

    @GET("profile/notify")
    suspend fun getNotify(
    ): Response<SetGetNotificationRequest>

    /**********************************************************************************************************************/

    @Multipart
    @POST("upload")
    suspend fun upload(
        @Part file: MultipartBody.Part
    ): Response<ImageResponse>

    @GET("upload/{id}")
    suspend fun uploadPredict(
        @Path("id") fileId: String
    ): Response<PredictionResponse>

    @DELETE("upload/{id}")
    suspend fun deleteUpload(
        @Path("id") fileId: String
    ): Response<Unit>

    /**********************************************************************************************************************/

    @POST("listing")
    suspend fun createListing(
       @Body request: CreateListingRequest
    ): Response<IdResponse>

    @PUT("listing/{id}")
    suspend fun updateListing(
        @Path("id") listingId: String,
        @Body request: CreateListingRequest
    ): Response<Unit>

    @GET("listing/{id}")
    suspend fun getListingDetail(
        @Path("id") listingId: String,
    ): Response<GetListingResponse>

    @PATCH("listing/{id}/status")
    suspend fun updateStatusListing(
        @Path("id") listingId: String,
        @Body request: UpdateListingStatusRequest
    ): Response<Unit>

    @DELETE("listing/{id}")
    suspend fun deleteListing(
        @Path("id") listingId: String,
    ): Response<Unit>

    @GET("listing")
    suspend fun listListing(
        @Query("query") query: String? = null,
        @Query("brand_id") brandId: String? = null,
        @Query("series_id") seriesId: String? = null,
        @Query("model_id") modelId: String? = null,
        @Query("body_type") bodyType: String? = null,
        @Query("drive_type") driveType: String? = null,
        @Query("transmission_type") transmissionType: String? = null,
        @Query("fuel_type") fuelType: String? = null,
        @Query("city") city: String? = null,
        @Query("district") district: String? = null,
        @Query("neighborhood") neighborhood: String? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_kilometers") minKilometers: Int? = null,
        @Query("max_kilometers") maxKilometers: Int? = null,
        @Query("min_engine_power") minEnginePower: Int? = null,
        @Query("max_engine_power") maxEnginePower: Int? = null,
        @Query("min_engine_volume") minEngineVolume: Int? = null,
        @Query("max_engine_volume") maxEngineVolume: Int? = null,
        @Query("color") color: String? = null,
        @Query("heavy_damage") heavyDamage: Boolean? = null,
        @Query("sort") sortBy: String? = null,
        @Query("order") sortOrder: String? = null,
        @Query("created_by") createdBy: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ) : Response<ListListingResponse>

    /**********************************************************************************************************************/

    @POST("favorite/{listing_id}")
    suspend fun createFavorite(
        @Path("listing_id") listingId: String
    ): Response<Unit>

    @DELETE("favorite/{listing_id}")
    suspend fun deleteFavorite(
        @Path("listing_id") listingId: String
    ): Response<Unit>

    @GET("favorite")
    suspend fun getFavorites(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ListListingResponse>

    /**********************************************************************************************************************/

    @GET("brand")
    suspend fun getBrands(): Response<GetBrandsResponse>

    @POST("brand")
    suspend fun createBrand(@Body brand: CreateBrandRequest): Response<IdResponse>

    @DELETE("brand/{id}")
    suspend fun deleteBrand(@Path("id") brandId: String): Response<Unit>

    @POST("brand/{id}/series")
    suspend fun createSeries(
        @Path("id") brandId: String,
        @Body series: NameRequest
    ): Response<IdResponse>

    @DELETE("brand/{id}/series/{sid}")
    suspend fun deleteSeries(
        @Path("id") brandId: String,
        @Path("sid") seriesId: String
    ): Response<Unit>

    @POST("brand/{id}/series/{sid}/model")
    suspend fun createModel(
        @Path("id") brandId: String,
        @Path("sid") seriesId: String,
        @Body model: NameRequest
    ): Response<IdResponse>

    @DELETE("brand/{id}/series/{sid}/model/{mid}")
    suspend fun deleteModel(
        @Path("id") brandId: String,
        @Path("sid") seriesId: String,
        @Path("mid") modelId: String
    ): Response<Unit>

    /**********************************************************************************************************************/
    @POST("chat/{listing_id}/{user_id}")
    suspend fun sendMessage(
        @Path("listing_id") listingId: String,
        @Path("user_id") userId: String,
        @Body request: SendMessageRequest
    ): Response<Unit>

    @GET("chat/{listing_id}/{user_id}")
    suspend fun getMessages(
        @Path("listing_id") listingId: String,
        @Path("user_id") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetMessageResponse>

    @GET("chat")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetChatResponse>

    /**********************************************************************************************************************/
    @GET("user/{user_id}")
    suspend fun getUserInfo(
        @Path("user_id") userId: String,
    ): Response<UserInfo>

    /**********************************************************************************************************************/
    @POST("predict")
    suspend fun pricePredict(
        @Body request: PricePredictionRequest
    ): Response<PricePredictionResponse>

    @GET("predict")
    suspend fun getPricePredictHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<GetPricePredictHistoryResponse>
    /**********************************************************************************************************************/

    @GET("notification")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<GetNotificationResponse>

    @DELETE("notification/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String,
    ): Response<Unit>

    @PUT("notification/{id}")
    suspend fun readNotification(
        @Path("id") id: String,
    ): Response<Unit>

    /**********************************************************************************************************************/

    @GET("admin/count")
    suspend fun getStats(
    ): Response<GetStatsResponse>

    @GET("admin/users")
    suspend fun getAllUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<GetUsersResponse>

    @DELETE("profile/{id}")
    suspend fun deleteUser(
        @Path("id") id: String,
    ): Response<Unit>

}