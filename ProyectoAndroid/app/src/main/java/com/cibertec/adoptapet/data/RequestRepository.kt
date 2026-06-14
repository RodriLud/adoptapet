package com.cibertec.adoptapet.data

import com.cibertec.adoptapet.models.AdoptionRequest

object RequestRepository {
    private val listaSolicitudes = mutableListOf(
        AdoptionRequest(
            id = 1,
            petId = 1,
            petName = "Luna",
            applicantName = "María López",
            dni = "76543210",
            phone = "999888777",
            email = "maria@correo.com",
            address = "Av. Los Jardines 123",
            reason = "Deseo adoptar porque tengo espacio y tiempo para cuidar una mascota.",
            date = "06/06/2026",
            status = "Pendiente",
            hasExperience = "yes",
        )
    )

    val solicitudes: List<AdoptionRequest>
        get() = listaSolicitudes

    fun agregarSolicitud(solicitud: AdoptionRequest) {
        listaSolicitudes.add(solicitud)
    }

    fun generarNuevoId(): Int {
        return if (listaSolicitudes.isEmpty()) {
            1
        } else {
            listaSolicitudes.maxOf { it.id } + 1
        }
    }

    fun buscarPorId(id: Int): AdoptionRequest? {
        return listaSolicitudes.find { it.id == id }
    }
}