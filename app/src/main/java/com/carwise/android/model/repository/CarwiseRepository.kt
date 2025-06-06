package com.carwise.android.model.repository

import com.carwise.android.data.model.CreateBrandRequest
import com.carwise.android.data.model.CreateListingRequest
import com.carwise.android.data.model.ForgotPasswordRequest
import com.carwise.android.data.model.GetBrandsResponse
import com.carwise.android.data.model.GetListingResponse
import com.carwise.android.data.model.IdResponse
import com.carwise.android.data.model.ImageResponse
import com.carwise.android.data.model.ListListingResponse
import com.carwise.android.data.model.LoginRequest
import com.carwise.android.data.model.LoginResponse
import com.carwise.android.data.model.NameRequest
import com.carwise.android.data.model.PredictionResponse
import com.carwise.android.data.model.ProfileResponse
import com.carwise.android.data.model.RegisterRequest
import com.carwise.android.data.model.ResetPasswordQuery
import com.carwise.android.data.model.ResetPasswordRequest
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.UpdateListingStatusRequest
import com.carwise.android.data.model.User
import com.carwise.android.data.model.UserPayload
import okhttp3.MultipartBody
import java.io.File
import android.content.Context
import android.net.Uri
import com.carwise.android.data.model.GetChatResponse
import com.carwise.android.data.model.GetMessageResponse
import com.carwise.android.data.model.GetNotificationResponse
import com.carwise.android.data.model.GetPricePredictHistoryResponse
import com.carwise.android.data.model.PricePredictionRequest
import com.carwise.android.data.model.PricePredictionResponse
import com.carwise.android.data.model.SetGetNotificationRequest
import com.carwise.android.data.model.UserInfo

interface CarwiseRepository {
    /*-----------AUTH SERVİCE-------------*/
    suspend fun login(request: LoginRequest): ResultState<LoginResponse>
    suspend fun register(request: RegisterRequest): ResultState<LoginResponse>

    suspend fun forgotPassword(request: ForgotPasswordRequest): ResultState<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest, query: ResetPasswordQuery): ResultState<Unit>

    suspend fun logout()
    suspend fun isLoggedIn(): Boolean

    suspend fun loginWithGoogle(idToken:String): ResultState<LoginResponse>
    fun getCurrentUser(): UserPayload?

    /*-----------BRAND SERVICE-------------*/
    suspend fun getBrands(): ResultState<GetBrandsResponse>
    suspend fun createBrand(brand: CreateBrandRequest): ResultState<IdResponse>
    suspend fun deleteBrand(brandId: String): ResultState<Unit>

    suspend fun createSeries(brandId: String, series: NameRequest): ResultState<IdResponse>
    suspend fun deleteSeries(brandId: String, seriesId: String): ResultState<Unit>

    suspend fun createModel(brandId: String, seriesId: String, model: NameRequest): ResultState<IdResponse>
    suspend fun deleteModel(brandId: String, seriesId: String, modelId: String): ResultState<Unit>

    /*-----------Chat SERVICE-------------*/
    suspend fun sendMessage(
        listingId: String,
        userId: String,
        message: String
    ) : ResultState<Unit>

    suspend fun getMessages(
        listingId: String,
        userId: String,
        page: Int = 1,
        limit: Int = 20
    ) : ResultState<GetMessageResponse>

    suspend fun getChats(
        page: Int = 1,
        limit: Int = 20
    ) : ResultState<GetChatResponse>


    /*-----------UPLOAD SERVICE-------------*/
    suspend fun upload(file: MultipartBody.Part): ResultState<ImageResponse>
    suspend fun uploadPredict(fileId: String): ResultState<PredictionResponse>
    suspend fun deleteUpload(fileId: String): ResultState<Unit>

    /*-----------LISTING SERVICE-------------*/
    suspend fun createListing(request: CreateListingRequest): ResultState<IdResponse>
    suspend fun updateListing(listingId: String, request: CreateListingRequest): ResultState<Unit>
    suspend fun getListingDetail(listingId: String): ResultState<GetListingResponse>
    suspend fun updateStatusListing(listingId: String, request: UpdateListingStatusRequest): ResultState<Unit>
    suspend fun deleteListing(listingId: String): ResultState<Unit>
    suspend fun listListing(
        query: String? = null,
        brandId: String? = null,
        seriesId: String? = null,
        modelId: String? = null,
        bodyType: String? = null,
        driveType: String? = null,
        transmissionType: String? = null,
        fuelType: String? = null,
        city: String? = null,
        district: String? = null,
        neighborhood: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        minYear: Int? = null,
        maxYear: Int? = null,
        minKilometers: Int? = null,
        maxKilometers: Int? = null,
        minEnginePower: Int? = null,
        maxEnginePower: Int? = null,
        minEngineVolume: Int? = null,
        maxEngineVolume: Int? = null,
        color: String? = null,
        heavyDamage: Boolean? = null,
        sortBy: String? = null,
        sortOrder: String? = null,
        createdBy: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): ResultState<ListListingResponse>

    /*-----------FAVORITE SERVICE-------------*/
    suspend fun createFavorite(listingId: String): ResultState<Unit>
    suspend fun deleteFavorite(listingId: String): ResultState<Unit>
    suspend fun getFavorites(page: Int = 1, limit: Int = 10): ResultState<ListListingResponse>
    /*-----------PROFILE SERVICE-------------*/
    suspend fun getProfileDetail(): ResultState<ProfileResponse>
    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        countryCode: String,
        phoneNumber: String,
        avatarFile: File? = null
    ): ResultState<ProfileResponse>

    suspend fun setNotify(
        deviceToken: String,
        emailNotify: Boolean,
        pushNotify: Boolean
    ): ResultState<Unit>

    suspend fun getNotify(): ResultState<SetGetNotificationRequest>

    /*-----------User SERVICE-------------*/
    suspend fun getUserInfo(userId: String) : ResultState<UserInfo>

    /*-----------Price Predict SERVICE-------------*/
    suspend fun pricePredict(request: PricePredictionRequest): ResultState<PricePredictionResponse>
    suspend fun getPredictionHistory(page: Int = 1, limit: Int = 20): ResultState<GetPricePredictHistoryResponse>

    /*-----------Notification SERVICE-------------*/
    suspend fun getNotifications(page: Int = 1, limit: Int = 10): ResultState<GetNotificationResponse>
    suspend fun deleteNotification(id: String): ResultState<Unit>
    suspend fun readNotification(id: String): ResultState<Unit>



}