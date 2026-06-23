package com.cibertec.adoptapet.models

data class Solicitud(
    val id_solicitud: Int?,
    val fecha_registro: String?,
    val adoptante: Usuario?,
    val mascota: Mascota?,
    val comentario: String?,
    val motivo_adopcion: String?,
    val tipo_vivienda: String?,
    val experiencia_mascotas: String?,
    val otras_mascotas: String?,
    val cantidad_personas_hogar: Int?,
    val comentarios_adicionales: String?,
    val estado_solicitud: String?,
    val motivo_rechazo: String?,
    val programacionEntrega: ProgramacionEntrega?
) {
    fun toAdoptionRequest(): AdoptionRequest {
        val adoptanteNombre = listOfNotNull(
            adoptante?.nom_adoptante,
            adoptante?.ape_adoptante
        ).joinToString(" ").ifBlank { "Adoptante" }

        return AdoptionRequest(
            id = id_solicitud ?: 0,
            petId = mascota?.id_mascota ?: 0,
            petName = mascota?.nombre ?: "Mascota",
            applicantName = adoptanteNombre,
            dni = adoptante?.dni.orEmpty(),
            phone = adoptante?.telefono.orEmpty(),
            email = adoptante?.email.orEmpty(),
            address = adoptante?.direccion.orEmpty(),
            hasExperience = experiencia_mascotas.orEmpty(),
            reason = motivo_adopcion.orEmpty(),
            date = fecha_registro.orEmpty(),
            status = estado_solicitud ?: "PENDIENTE",
            rejectionReason = motivo_rechazo,
            deliveryDate = programacionEntrega?.fecha_entrega,
            deliveryStart = programacionEntrega?.hora_inicio,
            deliveryEnd = programacionEntrega?.hora_fin,
            pickupLimit = programacionEntrega?.fecha_limite_recojo,
            scheduleStatus = programacionEntrega?.estado_programacion,
            comments = comentarios_adicionales ?: comentario
        )
    }
}
