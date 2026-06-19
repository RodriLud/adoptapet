package com.cibertec.adoptapet.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.cibertec.adoptapet.models.AppNotification

class NotificationDao(private val db: SQLiteDatabase) {

    fun insertar(titulo: String, cuerpo: String, fecha: String): Long {
        val values = ContentValues().apply {
            put("titulo", titulo)
            put("cuerpo", cuerpo)
            put("fecha", fecha)
            put("leida", 0)
        }
        return db.insert("notificaciones", null, values)
    }

    fun listarTodas(): List<AppNotification> {
        val lista = mutableListOf<AppNotification>()
        val cursor = db.query(
            "notificaciones",
            arrayOf("id", "titulo", "cuerpo", "fecha", "leida"),
            null,
            null,
            null,
            null,
            "id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    AppNotification(
                        id = it.getLong(0),
                        title = it.getString(1),
                        body = it.getString(2),
                        date = it.getString(3),
                        read = it.getInt(4) == 1
                    )
                )
            }
        }
        return lista
    }

    fun marcarLeida(id: Long) {
        val values = ContentValues().apply { put("leida", 1) }
        db.update("notificaciones", values, "id = ?", arrayOf(id.toString()))
    }
}
