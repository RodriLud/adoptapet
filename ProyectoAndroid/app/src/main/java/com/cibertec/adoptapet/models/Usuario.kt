package com.cibertec.adoptapet.models

data class Usuario(
    val id_usuario: Int,
    val username: String,
    val rol: String,
    val activo: Boolean?,
    val nom_adoptante: String?,
    val ape_adoptante: String?,
    val dni: String?,
    val fec_nacimiento: String?,
    val email: String?,
    val telefono: String?,
    val direccion: String?
) {
    fun nombreCompleto(): String {
        return listOfNotNull(nom_adoptante, ape_adoptante)
            .joinToString(" ")
            .ifBlank { username }
    }
}
