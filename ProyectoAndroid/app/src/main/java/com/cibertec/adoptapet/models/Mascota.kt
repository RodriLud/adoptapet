package com.cibertec.adoptapet.models

import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.network.ApiConfig

data class Mascota(
    val id_mascota: Int,
    val nombre: String?,
    val especie: String?,
    val raza: String?,
    val sexo: String?,
    val edad_anios: Int?,
    val edad_meses: Int?,
    val est_salud: String?,
    val est_adopcion: String?,
    val estado_esterilizacion: String?,
    val observaciones: String?,
    val ruta_imagen: String?,
    val version: Long?
) {
    fun estaDisponible(): Boolean {
        return est_adopcion.equals("DISPONIBLE", ignoreCase = true) &&
            !est_salud.equals("CRITICO", ignoreCase = true)
    }

    fun toPet(): Pet {
        val edad = when {
            (edad_anios ?: 0) > 0 && (edad_meses ?: 0) > 0 -> "${edad_anios} anios ${edad_meses} meses"
            (edad_anios ?: 0) > 0 -> "${edad_anios} anios"
            (edad_meses ?: 0) > 0 -> "${edad_meses} meses"
            else -> "Edad no indicada"
        }

        val esterilizado = estado_esterilizacion.equals("ESTERILIZADO", ignoreCase = true)
        val urlImagen = ruta_imagen
            ?.takeIf { it.isNotBlank() }
            ?.let { "${ApiConfig.BASE_URL}fotos_mascotas/$it" }

        return Pet(
            id = id_mascota,
            name = nombre.orEmpty(),
            type = especie.orEmpty(),
            age = edad,
            gender = sexo.orEmpty().lowercase().replaceFirstChar { it.uppercase() },
            size = raza.orEmpty().ifBlank { "Raza no indicada" },
            sterilized = esterilizado,
            description = observaciones.orEmpty().ifBlank { "Sin observaciones registradas." },
            imageRes = R.drawable.kitten1,
            imageUrl = urlImagen,
            breed = raza.orEmpty(),
            healthStatus = est_salud.orEmpty(),
            adoptionStatus = est_adopcion.orEmpty()
        )
    }
}
