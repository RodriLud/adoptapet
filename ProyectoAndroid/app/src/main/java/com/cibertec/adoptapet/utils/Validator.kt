package com.cibertec.adoptapet.utils
import android.util.Patterns

object Validador {

    fun correoValido(correo: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(correo).matches()
    }

    fun dniValido(dni: String): Boolean {
        return dni.length == 8 && dni.all { it.isDigit() }
    }

    fun telefonoValido(telefono: String): Boolean {
        return telefono.length == 9 && telefono.all { it.isDigit() }
    }

    fun passwordValida(password: String): Boolean {
        return password.length >= 6
    }

    fun textoMinimo(texto: String, minimo: Int): Boolean {
        return texto.trim().length >= minimo
    }
}