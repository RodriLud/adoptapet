package com.cibertec.adoptapet.network

import com.cibertec.adoptapet.models.Adoptante
import com.cibertec.adoptapet.models.Login
import com.cibertec.adoptapet.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("auth/login")
    fun login(@Body login: Login): Call<Usuario>

    @POST("auth/register")
    fun register(@Body adoptante: Adoptante): Call<Usuario>
}
