package com.cibertec.adoptapet.network

import com.cibertec.adoptapet.models.Solicitud
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface SolicitudService {

    @GET("solicitud")
    fun listarSolicitudes(): Call<List<Solicitud>>

    @GET("solicitud/{id}")
    fun buscarSolicitud(@Path("id") id: Int): Call<Solicitud>

    @Multipart
    @POST("solicitud/registrar")
    fun registrarSolicitud(
        @Part idAdoptante: MultipartBody.Part,
        @Part idMascota: MultipartBody.Part,
        @Part comentario: MultipartBody.Part,
        @Part motivoAdopcion: MultipartBody.Part,
        @Part tipoVivienda: MultipartBody.Part,
        @Part experienciaMascotas: MultipartBody.Part,
        @Part otrasMascotas: MultipartBody.Part,
        @Part cantidadPersonasHogar: MultipartBody.Part,
        @Part comentariosAdicionales: MultipartBody.Part,
        @Part archivoDni: MultipartBody.Part?,
        @Part archivoDomicilio: MultipartBody.Part?
    ): Call<Solicitud>
}
