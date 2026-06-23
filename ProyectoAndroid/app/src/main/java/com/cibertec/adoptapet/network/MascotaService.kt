package com.cibertec.adoptapet.network

import com.cibertec.adoptapet.models.Mascota
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MascotaService {

    @GET("mascota")
    fun listarMascotas(): Call<List<Mascota>>

    @GET("mascota/{id}")
    fun buscarMascota(@Path("id") id: Int): Call<Mascota>
}
