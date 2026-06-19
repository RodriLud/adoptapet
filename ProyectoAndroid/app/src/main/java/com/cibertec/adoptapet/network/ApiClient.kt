package com.cibertec.adoptapet.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private fun retrofit(username: String? = null, password: String? = null): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)

        if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            clientBuilder.addInterceptor(AuthInterceptor(username, password))
        }

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun authService(): AuthService {
        return retrofit().create(AuthService::class.java)
    }

    fun authService(username: String, password: String): AuthService {
        return retrofit(username, password).create(AuthService::class.java)
    }

    fun mascotaService(): MascotaService {
        return retrofit().create(MascotaService::class.java)
    }

    fun solicitudService(username: String, password: String): SolicitudService {
        return retrofit(username, password).create(SolicitudService::class.java)
    }
}
