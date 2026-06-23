package com.cibertec.adoptapet.models

data class Adoptante(
    val username: String,
    val password: String,
    val rol: String = "ROLE_ADOPTANTE",
    val nom_adoptante: String,
    val ape_adoptante: String,
    val dni: String,
    val fec_nacimiento: String,
    val email: String,
    val telefono: String,
    val direccion: String
)
