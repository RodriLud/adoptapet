package com.cibertec.adoptapet.data

import android.content.Context
import com.cibertec.adoptapet.models.Usuario

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("adoptapet_session", Context.MODE_PRIVATE)

    fun guardarSesion(user: Usuario, password: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED, true)
            .putInt(KEY_USER_ID, user.id_usuario)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_PASSWORD, password)
            .putString(KEY_ROLE, user.rol)
            .putString(KEY_NAME, user.nombreCompleto())
            .putString(KEY_EMAIL, user.email ?: user.username)
            .putString(KEY_DNI, user.dni.orEmpty())
            .putString(KEY_PHONE, user.telefono.orEmpty())
            .putString(KEY_ADDRESS, user.direccion.orEmpty())
            .apply()
    }

    fun haySesionActiva(): Boolean {
        return prefs.getBoolean(KEY_LOGGED, false)
    }

    fun obtenerUsername(): String {
        return prefs.getString(KEY_USERNAME, "") ?: ""
    }

    fun obtenerUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun obtenerPassword(): String {
        return prefs.getString(KEY_PASSWORD, "") ?: ""
    }

    fun obtenerRol(): String {
        return prefs.getString(KEY_ROLE, "") ?: ""
    }

    fun obtenerNombre(): String {
        return prefs.getString(KEY_NAME, "Usuario") ?: "Usuario"
    }

    fun obtenerEmail(): String {
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun obtenerDni(): String {
        return prefs.getString(KEY_DNI, "") ?: ""
    }

    fun obtenerTelefono(): String {
        return prefs.getString(KEY_PHONE, "") ?: ""
    }

    fun obtenerDireccion(): String {
        return prefs.getString(KEY_ADDRESS, "") ?: ""
    }

    fun actualizarUserId(nuevoId: Int) {
        prefs.edit().putInt(KEY_USER_ID, nuevoId).apply()
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_LOGGED = "logged"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ROLE = "role"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_DNI = "dni"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDRESS = "address"
    }
}
