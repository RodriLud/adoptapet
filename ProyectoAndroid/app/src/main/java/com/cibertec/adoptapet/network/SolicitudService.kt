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
        @Part("id_adoptante") idAdoptante: RequestBody,
        @Part("id_mascota") idMascota: RequestBody,
        @Part("comentario") comentario: RequestBody,
        @Part("motivo_adopcion") motivoAdopcion: RequestBody,
        @Part("tipo_vivienda") tipoVivienda: RequestBody,
        @Part("experiencia_mascotas") experienciaMascotas: RequestBody,
        @Part("otras_mascotas") otrasMascotas: RequestBody,
        @Part("cantidad_personas_hogar") cantidadPersonasHogar: RequestBody,
        @Part("comentarios_adicionales") comentariosAdicionales: RequestBody,
        @Part archivoDni: MultipartBody.Part?,
        @Part archivoDomicilio: MultipartBody.Part?
    ): Call<Solicitud>
}
