package com.cibertec.adoptapet.data

import android.content.Context
import com.cibertec.adoptapet.database.SolicitudLocalDao
import com.cibertec.adoptapet.models.AdoptionRequest
import com.cibertec.adoptapet.models.Solicitud
import com.cibertec.adoptapet.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object RequestRepository {

    var solicitudes: List<AdoptionRequest> = emptyList()
        private set

    fun cargarSolicitudes(
        context: Context,
        sessionManager: SessionManager,
        callback: (List<AdoptionRequest>, Boolean) -> Unit
    ) {
        val idUsuario = sessionManager.obtenerUserId()
        if (idUsuario <= 0) {
            cargarDesdeSqlite(context, callback)
            return
        }

        val username = sessionManager.obtenerUsername()
        val password = sessionManager.obtenerPassword()
        val service = if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
            ApiClient.solicitudService(username, password)
        } else {
            ApiClient.solicitudServicePublica()
        }

        service
            .listarSolicitudesPorAdoptante(idUsuario)
            .enqueue(object : Callback<List<Solicitud>> {
                override fun onResponse(
                    call: Call<List<Solicitud>>,
                    response: Response<List<Solicitud>>
                ) {
                    if (response.isSuccessful) {
                        val lista = response.body()
                            .orEmpty()
                            .map { it.toAdoptionRequest() }
                            .sortedByDescending { it.id }
                        solicitudes = lista
                        SolicitudLocalDao(context).guardarSolicitudes(lista)
                        callback(lista, true)
                    } else {
                        cargarDesdeSqlite(context, callback)
                    }
                }

                override fun onFailure(call: Call<List<Solicitud>>, t: Throwable) {
                    cargarDesdeSqlite(context, callback)
                }
            })
    }

    fun buscarSolicitud(
        context: Context,
        sessionManager: SessionManager,
        idSolicitud: Int,
        callback: (AdoptionRequest?) -> Unit
    ) {
        val service = ApiClient.solicitudService(
            sessionManager.obtenerUsername(),
            sessionManager.obtenerPassword()
        )

        service.buscarSolicitud(idSolicitud).enqueue(object : Callback<Solicitud> {
            override fun onResponse(call: Call<Solicitud>, response: Response<Solicitud>) {
                val solicitud = response.body()?.toAdoptionRequest()
                if (response.isSuccessful && solicitud != null) {
                    callback(solicitud)
                } else {
                    callback(SolicitudLocalDao(context).buscarSolicitud(idSolicitud))
                }
            }

            override fun onFailure(call: Call<Solicitud>, t: Throwable) {
                callback(SolicitudLocalDao(context).buscarSolicitud(idSolicitud))
            }
        })
    }

    private fun cargarDesdeSqlite(
        context: Context,
        callback: (List<AdoptionRequest>, Boolean) -> Unit
    ) {
        val locales = SolicitudLocalDao(context).listarSolicitudes()
        solicitudes = locales
        callback(locales, false)
    }
}
