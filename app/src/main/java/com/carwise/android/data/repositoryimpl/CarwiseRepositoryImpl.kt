package com.carwise.android.data.repositoryimpl

import android.content.Context
import android.util.Log
import com.carwise.android.data.local.AuthStorage.AuthStorage
import com.carwise.android.data.model.CreateBrandRequest
import com.carwise.android.data.model.CreateListingRequest
import com.carwise.android.data.model.ErrorResponse
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
import com.carwise.android.data.model.ResetPasswordQuery
import com.carwise.android.data.model.ResetPasswordRequest
import com.carwise.android.data.model.ResultState
import com.carwise.android.data.model.SendMessageRequest
import com.carwise.android.data.model.SetGetNotificationRequest
import com.carwise.android.data.model.UpdateListingStatusRequest
import com.carwise.android.data.model.UpdateRoleRequest
import com.carwise.android.data.model.UserInfo
import com.carwise.android.data.model.UserPayload
import com.carwise.android.data.remote.CarwiseService
import com.carwise.android.model.repository.CarwiseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class CarwiseRepositoryImpl @Inject constructor(
    private val carwiseService: CarwiseService,
    private val authStorage: AuthStorage,
    @ApplicationContext private val context: Context
) : CarwiseRepository {
    override suspend fun login(request: LoginRequest): ResultState<LoginResponse> {
        return try {
            Log.d("CarwiseRepositoryImpl", "Login request: $request")
            val response = carwiseService.login(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    authStorage.saveLoginResponse(it)
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "E-posta adresiniz veya şifreniz hatalı. Lütfen kontrol edip tekrar deneyin."
                    )
                )
            } else {
                Log.d("CarwiseRepositoryImpl", "Login response: ${response.body()}")
                ResultState.Error(
                    ErrorResponse(
                        error = "E-posta adresiniz veya şifreniz hatalı. Lütfen kontrol edip tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("CarwiseRepositoryImpl", "Login error: ${e.message}")
            ResultState.Error(
                ErrorResponse(
                    error = "E-posta adresiniz veya şifreniz hatalı. Lütfen kontrol edip tekrar deneyin."
                )
            )
        }
    }

    override suspend fun register(request: RegisterRequest): ResultState<LoginResponse> {
        return try {
            Log.d("CarwiseRepositoryImpl", "register request: ${request}")
            val response = carwiseService.register(request)
            if (response.isSuccessful) {
                Log.d("CarwiseRepositoryImpl", "register response: ${response.body()}")
                response.body()?.let {
                    authStorage.saveLoginResponse(it)
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bu e-posta adresi zaten kullanılıyor. Lütfen farklı bir e-posta adresi deneyin."
                    )
                )
            } else {
                Log.d("CarwiseRepositoryImpl", "register response: ${response.body()}")
                ResultState.Error(
                    ErrorResponse(
                        error = "Bu e-posta adresi zaten kullanılıyor. Lütfen farklı bir e-posta adresi deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("CarwiseRepositoryImpl", "Login response: ${e.message}")
            ResultState.Error(
                ErrorResponse(
                    error = "Bu e-posta adresi zaten kullanılıyor. Lütfen farklı bir e-posta adresi deneyin."
                )
            )
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ResultState<Unit> {
        return try {
            val response = carwiseService.forgotPassword(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                )
            )
        }

    }

    override suspend fun resetPassword(
        request: ResetPasswordRequest,
        query: ResetPasswordQuery
    ): ResultState<Unit> {
        return try {
            val response = carwiseService.resetPasword(
                request = request,
                token = query.token,
                email = query.email
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Şİfre sıfırlama isteği başarısız oldu. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun logout() {
        authStorage.clearAuthData()
    }

    override suspend fun isLoggedIn(): Boolean {
        if (authStorage.getAccessToken().isNotEmpty()) {
            return true
        }
        return false
    }

    override suspend fun loginWithGoogle(idToken: String): ResultState<LoginResponse> {
        return try {
            val response = carwiseService.loginWithGoogle(
                idToken = idToken
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    authStorage.saveLoginResponse(it)
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Google hesabınıza giriş başarısız oldu. Lütfen daha sonra tekrar deneyin"
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Google hesabınıza giriş başarısız oldu. Lütfen daha sonra tekrar deneyin"
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Google hesabınıza giriş başarısız oldu. Lütfen daha sonra tekrar deneyin"
                )
            )
        }
    }

    override suspend fun getBrands(): ResultState<GetBrandsResponse> {
        return try {
            val response = carwiseService.getBrands()
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Marka listesi alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Marka listesi alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Marka listesi alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun createBrand(brand: CreateBrandRequest): ResultState<IdResponse> {
        return try {
            val response = carwiseService.createBrand(brand)
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Marka oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Marka oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Marka oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteBrand(brandId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteBrand(brandId)
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Marka silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Marka silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Marka silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun createSeries(
        brandId: String,
        series: NameRequest
    ): ResultState<IdResponse> {
        return try {
            val response = carwiseService.createSeries(
                brandId = brandId,
                series = series
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Seri oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Seri oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Seri oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteSeries(brandId: String, seriesId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteSeries(
                brandId = brandId,
                seriesId = seriesId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Seri silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Seri silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Seri silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun createModel(
        brandId: String,
        seriesId: String,
        model: NameRequest
    ): ResultState<IdResponse> {
        return try {
            val response = carwiseService.createModel(
                brandId = brandId,
                seriesId = seriesId,
                model = model
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Model oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Model oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Model oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteModel(
        brandId: String,
        seriesId: String,
        modelId: String
    ): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteModel(
                brandId = brandId,
                seriesId = seriesId,
                modelId = modelId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Model silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Model silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Model silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun sendMessage(
        listingId: String,
        userId: String,
        message: String
    ): ResultState<Unit> {
        return try {
            val response = carwiseService.sendMessage(
                listingId,
                userId,
                SendMessageRequest(message)
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Mesaj gönderilemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Mesaj gönderilemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Mesaj gönderilemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getMessages(
        listingId: String,
        userId: String,
        page: Int,
        limit: Int
    ): ResultState<GetMessageResponse> {
        return try {
            val response = carwiseService.getMessages(
                listingId,
                userId,
                page, limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getChats(page: Int, limit: Int): ResultState<GetChatResponse> {
        return try {
            val response = carwiseService.getChats(
                page, limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Mesajlar alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun upload(file: MultipartBody.Part): ResultState<ImageResponse> {
        return try {
            val originalFilename = file.headers?.get("Content-Disposition")?.let { disposition ->
                Regex("filename=\"(.+?)\"").find(disposition)?.groupValues?.get(1)
            }
            
            val allowedExtensions = listOf("jpg", "jpeg", "png", "gif")
            val fileExtension = originalFilename?.substringAfterLast('.', "")?.lowercase()
            
            if (fileExtension == null || fileExtension !in allowedExtensions) {
                return ResultState.Error(
                    ErrorResponse("Sadece JPEG, JPG, PNG ve GIF formatında resimler yüklenebilir.")
                )
            }

            val response = carwiseService.upload(file)
            if (response.isSuccessful) {
                response.body()?.let { imageResponse ->
                    ResultState.Success(imageResponse)
                } ?: ResultState.Error(ErrorResponse("Sunucudan yanıt alınamadı"))
            } else {
                val errorBody = response.errorBody()?.string()
                val error = try {
                    Json.decodeFromString<ErrorResponse>(errorBody ?: "").error
                } catch (e: Exception) {
                    "Fotoğraf yüklenirken bir hata oluştu"
                }
                ResultState.Error(ErrorResponse(error))
            }
        } catch (e: Exception) {
            ResultState.Error(ErrorResponse("Fotoğraf yüklenirken bir hata oluştu: ${e.message}"))
        }
    }

    override suspend fun uploadPredict(fileId: String): ResultState<PredictionResponse> {
        return try {
            val response = carwiseService.uploadPredict(
                fileId = fileId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Resim tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Resim tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                        )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Resim tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteUpload(fileId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteUpload(
                fileId = fileId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Resim silinemedi. Lütfen daha sonra tekrar deneyin."   
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Resim silinemedi. Lütfen daha sonra tekrar deneyin."   
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Resim silinemedi. Lütfen daha sonra tekrar deneyin."      
                )
            )
        }
    }

    override suspend fun createListing(request: CreateListingRequest): ResultState<IdResponse> {
        return try {
            val response = carwiseService.createListing(
                request = request
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                        )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan oluşturulamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun updateListing(
        listingId: String,
        request: CreateListingRequest
    ): ResultState<Unit> {
            return try {
            val response = carwiseService.updateListing(
                listingId = listingId,
                request = request
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan güncellenemedi. Lütfen daha sonra tekrar deneyin."    
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan güncellenemedi. Lütfen daha sonra tekrar deneyin."    
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan güncellenemedi. Lütfen daha sonra tekrar deneyin."        
                )
            )
        }
    }

    override suspend fun getListingDetail(listingId: String): ResultState<GetListingResponse> {
        return try {
            val response = carwiseService.getListingDetail(
                listingId = listingId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan detayı alınamadı. Lütfen daha sonra tekrar deneyin."
                        )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan detayı alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan detayı alınamadı. Lütfen daha sonra tekrar deneyin."
                )   
            )
        }
    }

    override suspend fun updateStatusListing(
        listingId: String,
        request: UpdateListingStatusRequest
    ): ResultState<Unit> {
        return try {
            val response = carwiseService.updateStatusListing(
                listingId = listingId,
                request = request
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it) 
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan durumu güncellenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(  
                    ErrorResponse(
                        error = "İlan durumu güncellenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {    
            ResultState.Error(
                ErrorResponse(
                    error = "İlan durumu güncellenemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteListing(listingId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteListing(
                listingId = listingId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan silinemedi. Lütfen daha sonra tekrar deneyin."    
                    )
                )
            } else {
                Log.d("deleteListing", "Response code: ${response.code()}, Error body: ${response.errorBody()?.string() ?: "No error body"}")
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan silinemedi. Lütfen daha sonra tekrar deneyin."    
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan silinemedi. Lütfen daha sonra tekrar deneyin."    
                )
            )
        }
    }

    override suspend fun listListing(
        query: String?,
        brandId: String?,
        seriesId: String?,
        modelId: String?,
        bodyType: String?,
        driveType: String?,
        transmissionType: String?,
        fuelType: String?,
        city: String?,
        district: String?,
        neighborhood: String?,
        minPrice: Int?,
        maxPrice: Int?,
        minYear: Int?,
        maxYear: Int?,
        minKilometers: Int?,
        maxKilometers: Int?,
        minEnginePower: Int?,
        maxEnginePower: Int?,
        minEngineVolume: Int?,
        maxEngineVolume: Int?,
        color: String?,
        heavyDamage: Boolean?,
        sortBy: String?,
        sortOrder: String?,
        createdBy: String?,
        status: String?,
        page: Int,
        limit: Int
    ): ResultState<ListListingResponse> {
        return try {
            val response = carwiseService.listListing(
                query = query,
                brandId = brandId,
                seriesId = seriesId,
                modelId = modelId,
                bodyType = bodyType,
                driveType = driveType,
                transmissionType = transmissionType,
                fuelType = fuelType,
                city = city,
                district = district,
                neighborhood = neighborhood,
                minPrice = minPrice,
                maxPrice = maxPrice,
                minYear = minYear,
                maxYear = maxYear,
                minKilometers = minKilometers,
                maxKilometers = maxKilometers,
                minEnginePower = minEnginePower,
                maxEnginePower = maxEnginePower,
                minEngineVolume = minEngineVolume,
                maxEngineVolume = maxEngineVolume,
                color = color,
                heavyDamage = heavyDamage,
                sortBy = sortBy,
                sortOrder = sortOrder,
                createdBy = createdBy,
                status = status,
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                response.body()?.let {  
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan listelenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan listelenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan listelenemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }
    override suspend fun createFavorite(listingId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.createFavorite(
                listingId = listingId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan favorilere eklenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan favorilere eklenemedi. Lütfen daha sonra tekrar deneyin."
                        )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan favorilere eklenemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteFavorite(listingId: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteFavorite(
                listingId = listingId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "İlan favorilerden silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "İlan favorilerden silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "İlan favorilerden silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getFavorites(page: Int, limit: Int): ResultState<ListListingResponse> {
        return try {
            val response = carwiseService.getFavorites(
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Favori ilanlar alınamadı. Lütfen daha sonra tekrar deneyin."   
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Favori ilanlar alınamadı. Lütfen daha sonra tekrar deneyin."   
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Favori ilanlar alınamadı. Lütfen daha sonra tekrar deneyin."   
                )
            )
        }
    }

    override fun getCurrentUser(): UserPayload? {
        return authStorage.getCurrentUserPayload()
    }

    override suspend fun getProfileDetail(): ResultState<ProfileResponse> {
        return try {
            val response = carwiseService.getProfile()
            if (response.isSuccessful) {
                response.body()?.let { profileResponse ->
                    try {
                        // Get user's listings count
                        val listingsResponse = carwiseService.listListing(
                            createdBy = profileResponse.id,
                            page = 1,
                            limit = 1
                        )
                        
                        if (!listingsResponse.isSuccessful) {
                            Log.w("CarwiseRepositoryImpl", "Failed to get listings count: ${listingsResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("CarwiseRepositoryImpl", "Error getting listings count: ${e.message}", e)
                    }
                    
                    ResultState.Success(profileResponse)
                } ?: ResultState.Error(
                    ErrorResponse(
                        error = "Profil bilgileri alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                Log.d("deleteListing", "Response code: ${response.code()}, Error body: ${response.errorBody()?.string() ?: "No error body"}")
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Json.decodeFromString<ErrorResponse>(errorBody ?: "").error
                } catch (e: Exception) {
                    "Profil bilgileri alınamadı. Lütfen daha sonra tekrar deneyin."
                }
                ResultState.Error(ErrorResponse(error = errorMessage))
            }
        } catch (e: Exception) {
            Log.e("CarwiseRepositoryImpl", "getProfileDetail error: ${e.message}", e)
            ResultState.Error(
                ErrorResponse(
                    error = "Profil bilgileri alınamadı: ${e.message}"
                )
            )
        }
    }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        countryCode: String,
        phoneNumber: String,
        avatarFile: File?
    ): ResultState<ProfileResponse> {
        return try {
            // Input validation
            if (firstName.isBlank() || lastName.isBlank() || countryCode.isBlank() || phoneNumber.isBlank()) {
                return ResultState.Error(ErrorResponse(error = "Tüm alanları doldurunuz."))
            }

            // Phone number validation
            if (!phoneNumber.matches(Regex("^[0-9]{10}$"))) {
                return ResultState.Error(ErrorResponse(error = "Geçerli bir telefon numarası giriniz."))
            }

            // Create RequestBody instances for text fields
            val firstNameBody = firstName.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            val lastNameBody = lastName.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            val countryCodeBody = countryCode.trim().toRequestBody("text/plain".toMediaTypeOrNull())
            val phoneNumberBody = phoneNumber.trim().toRequestBody("text/plain".toMediaTypeOrNull())

            // Create MultipartBody.Part for avatar if file exists
            val avatarPart = avatarFile?.let { file ->
                if (!file.exists()) {
                    return ResultState.Error(ErrorResponse(error = "Seçilen dosya bulunamadı."))
                }
                if (file.length() > 5 * 1024 * 1024) { // 5MB limit
                    return ResultState.Error(ErrorResponse(error = "Dosya boyutu 5MB'dan büyük olamaz."))
                }
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", file.name, requestBody)
            }

            Log.d("CarwiseRepositoryImpl", "Updating profile with: firstName=$firstName, lastName=$lastName, countryCode=$countryCode, phoneNumber=$phoneNumber, hasAvatar=${avatarFile != null}")

            val response = carwiseService.updateProfile(
                firstName = firstNameBody,
                lastName = lastNameBody,
                countryCode = countryCodeBody,
                phoneNumber = phoneNumberBody,
                avatar = avatarPart
            )

            if (response.isSuccessful) {
                // Get updated profile data
                val profileResponse = carwiseService.getProfile()
                if (profileResponse.isSuccessful) {
                    profileResponse.body()?.let {
                        Log.d("CarwiseRepositoryImpl", "Profile updated successfully")
                        ResultState.Success(it)
                    } ?: run {
                        Log.e("CarwiseRepositoryImpl", "Profile update successful but getProfile returned null body")
                        ResultState.Error(
                            ErrorResponse(
                                error = "Profil güncellendi fakat yeni bilgiler alınamadı."
                            )
                        )
                    }
                } else {
                    val errorBody = profileResponse.errorBody()?.string()
                    Log.e("CarwiseRepositoryImpl", "Profile update successful but getProfile failed: ${profileResponse.code()}, error: $errorBody")
                    ResultState.Error(
                        ErrorResponse(
                            error = "Profil güncellendi fakat yeni bilgiler alınamadı."
                        )
                    )
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    Json.decodeFromString<ErrorResponse>(errorBody ?: "").error
                } catch (e: Exception) {
                    "Profil güncellenemedi. Lütfen daha sonra tekrar deneyin."
                }
                Log.e("CarwiseRepositoryImpl", "Profile update failed: code=${response.code()}, error=$errorMessage")
                ResultState.Error(ErrorResponse(error = errorMessage))
            }
        } catch (e: Exception) {
            Log.e("CarwiseRepositoryImpl", "updateProfile error: ${e.message}", e)
            ResultState.Error(
                ErrorResponse(
                    error = "Profil güncellenemedi: ${e.message}"
                )
            )
        }
    }

    override suspend fun setNotify(
        deviceToken: String,
        emailNotify: Boolean,
        pushNotify: Boolean
    ): ResultState<Unit> {
        return try {
            val response = carwiseService.setNotify(
                request = SetGetNotificationRequest(
                    deviceToken = deviceToken,
                    emailNotify = emailNotify,
                    pushNotify = pushNotify
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim Terchileri ayarlanamadı, lutfen tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim Terchileri ayarlanamadı, lutfen tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirim Terchileri ayarlanamadı, lutfen tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getNotify(): ResultState<SetGetNotificationRequest> {
        return try {
            val response = carwiseService.getNotify()
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim Terchileri alınamadı, lutfen tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim Terchileri alınamadı, lutfen tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirim Terchileri alınamadı, lutfen tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getUserInfo(userId: String): ResultState<UserInfo> {
        return try {
            val response = carwiseService.getUserInfo(
                userId = userId
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı bilgileri alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı bilgileri alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Kullanıcı bilgileri alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun pricePredict(request: PricePredictionRequest): ResultState<PricePredictionResponse> {
        return try {
            val response = carwiseService.pricePredict(
                request = request
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Fiyat tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Fiyat tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Fiyat tahmini yapılamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getPredictionHistory(
        page: Int,
        limit: Int
    ): ResultState<GetPricePredictHistoryResponse> {
        return try {
            val response = carwiseService.getPricePredictHistory(
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Tahmin geçmişi alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Tahmin geçmişi alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Tahmin geçmişi alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getNotifications(
        page: Int,
        limit: Int
    ): ResultState<GetNotificationResponse> {
        return try {
            val response = carwiseService.getNotifications(
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirimler alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteNotification(id: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteNotification(
                id = id
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirim silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun readNotification(id: String): ResultState<Unit> {
        return try {
            val response = carwiseService.readNotification(
               id = id
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim okunamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirim okunamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirim okunamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun readAllNotification(): ResultState<Unit> {
        return try {
            val response = carwiseService.readAllNotification()
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler okunamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler okunamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirimler okunamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteAllNotification(): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteAllNotification()
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bildirimler silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bildirimler silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getStats(): ResultState<GetStatsResponse> {
        return try {
            val response = carwiseService.getStats()
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun getUsers(page: Int, limit: Int): ResultState<GetUsersResponse> {
        return try {
            val response = carwiseService.getAllUsers(
                page = page,
                limit = limit
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Bilgiler alınamadı. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun deleteUser(id: String): ResultState<Unit> {
        return try {
            val response = carwiseService.deleteUser(
              id = id
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı Silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı Silinemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Kullanıcı Silinemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }

    override suspend fun updateRole(userId: String, role: Int): ResultState<Unit> {
        return try {
            val response = carwiseService.updateRole(
                request = UpdateRoleRequest(
                    userId = userId,
                    role = role
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ResultState.Success(it)
                } ?:  ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı Rolü güncellenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            } else {
                ResultState.Error(
                    ErrorResponse(
                        error = "Kullanıcı Rolü güncellenemedi. Lütfen daha sonra tekrar deneyin."
                    )
                )
            }
        } catch (e: Exception) {
            ResultState.Error(
                ErrorResponse(
                    error = "Kullanıcı Rolü güncellenemedi. Lütfen daha sonra tekrar deneyin."
                )
            )
        }
    }
}