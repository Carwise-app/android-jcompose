package com.carwise.android.di

import android.content.Context
import android.os.Build
import com.carwise.android.data.local.AuthStorage.AuthStorage
import com.carwise.android.data.remote.CarwiseService
import com.carwise.android.data.repositoryimpl.CarwiseRepositoryImpl
import com.carwise.android.model.repository.CarwiseRepository
import com.carwise.android.data.repository.LocationRepository
import com.carwise.android.util.NotificationManager
import com.carwise.android.data.remote.WebSocketService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAuthStorage(
        @ApplicationContext context: Context,
    ): AuthStorage {
        return AuthStorage(context)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthenticatedClient")
    fun provideAuthenticatedOkHttpClient(
        authStorage: AuthStorage
    ): OkHttpClient {
        val baseClient = getUnsafeOkHttpClient()
        return baseClient.newBuilder()
            .addInterceptor { chain ->
                val userAgent = getUserAgent()
                val request: Request = chain.request().newBuilder()
                    .addHeader("User-Agent", userAgent)
                    .build()
                chain.proceed(request)
            }.addInterceptor { chain ->
                val originalRequest = chain.request()
                var accessToken = ""

                runBlocking {
                    accessToken = authStorage.getAccessToken()
                }

                val requestWithAuth = if (accessToken.isNotEmpty()) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                } else {
                    originalRequest
                }

                chain.proceed(requestWithAuth)
            }
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthenticatedRetrofit")
    fun provideAuthenticatedRetrofit(@Named("AuthenticatedClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://carwisegw.yusuftalhaklc.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideCarwiseService(@Named("AuthenticatedRetrofit") retrofit: Retrofit): CarwiseService {
        return retrofit.create(CarwiseService::class.java)
    }

    @Provides
    @Singleton
    fun provideCarwiseRepository(
        carwiseService: CarwiseService,
        authStorage: AuthStorage,
        @ApplicationContext context: Context
    ): CarwiseRepository {
        return CarwiseRepositoryImpl(carwiseService, authStorage, context)
    }

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository {
        return LocationRepository(context)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        repository: CarwiseRepository
    ): NotificationManager {
        return NotificationManager(context, repository)
    }

    @Provides
    @Singleton
    fun provideWebSocketService(authStorage: AuthStorage): WebSocketService {
        return WebSocketService(authStorage)
    }
}

fun getUserAgent(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val version = Build.VERSION.RELEASE
    val userAgent = "Carwise/Anroid (Android; ${manufacturer} ${model}; Android $version)"

    return userAgent
}
