package com.cibertec.adoptapet.database

import android.content.ContentValues
import android.content.Context
import com.cibertec.adoptapet.models.AdoptionRequest
import com.cibertec.adoptapet.models.Solicitud
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SolicitudLocalDao(context: Context) {

    private val dbHelper = AdoptaPetDatabase(context)

    fun guardarBorrador(
        idMascota: Int,
        motivo: String,
        vivienda: String,
        experiencia: String,
        otrasMascotas: String,
        personas: Int,
        comentarios: String
    ) {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("id_mascota", idMascota)
            put("motivo_adopcion", motivo)
            put("tipo_vivienda", vivienda)
            put("experiencia_mascotas", experiencia)
            put("otras_mascotas", otrasMascotas)
            put("cantidad_personas_hogar", personas)
            put("comentarios_adicionales", comentarios)
            put("fecha_guardado", fechaActual())
        }
        db.insert("borradores_solicitud", null, valores)
        db.close()
    }

    fun guardarSolicitudPendiente(solicitud: Solicitud, idMascota: Int, nombreMascota: String) {
        val db = dbHelper.writableDatabase
        val valores = ContentValues().apply {
            put("id_solicitud", solicitud.id_solicitud ?: System.currentTimeMillis().toInt())
            put("id_mascota", idMascota)
            put("nombre_mascota", nombreMascota)
            put("fecha_registro", solicitud.fecha_registro ?: fechaActual())
            put("estado_solicitud", solicitud.estado_solicitud ?: "PENDIENTE")
            put("fecha_entrega", solicitud.programacionEntrega?.fecha_entrega)
            put("hora_inicio", solicitud.programacionEntrega?.hora_inicio)
            put("hora_fin", solicitud.programacionEntrega?.hora_fin)
            put("motivo_rechazo", solicitud.motivo_rechazo)
        }
        db.insertWithOnConflict("solicitudes", null, valores, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun guardarSolicitudes(solicitudes: List<AdoptionRequest>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("solicitudes", null, null)
            solicitudes.forEach { solicitud ->
                val valores = ContentValues().apply {
                    put("id_solicitud", solicitud.id)
                    put("id_mascota", solicitud.petId)
                    put("nombre_mascota", solicitud.petName)
                    put("fecha_registro", solicitud.date)
                    put("estado_solicitud", solicitud.status)
                    put("fecha_entrega", solicitud.deliveryDate)
                    put("hora_inicio", solicitud.deliveryStart)
                    put("hora_fin", solicitud.deliveryEnd)
                    put("motivo_rechazo", solicitud.rejectionReason)
                }
                db.insertWithOnConflict("solicitudes", null, valores, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun listarSolicitudes(): List<AdoptionRequest> {
        val resultado = mutableListOf<AdoptionRequest>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM solicitudes ORDER BY id_solicitud DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                resultado.add(
                    AdoptionRequest(
                        id = it.getInt(it.getColumnIndexOrThrow("id_solicitud")),
                        petId = it.getInt(it.getColumnIndexOrThrow("id_mascota")),
                        petName = it.getString(it.getColumnIndexOrThrow("nombre_mascota")) ?: "Mascota",
                        applicantName = "Adoptante",
                        dni = "",
                        phone = "",
                        email = "",
                        address = "",
                        hasExperience = "",
                        reason = "",
                        date = it.getString(it.getColumnIndexOrThrow("fecha_registro")) ?: "",
                        status = it.getString(it.getColumnIndexOrThrow("estado_solicitud")) ?: "PENDIENTE",
                        rejectionReason = it.getString(it.getColumnIndexOrThrow("motivo_rechazo")),
                        deliveryDate = it.getString(it.getColumnIndexOrThrow("fecha_entrega")),
                        deliveryStart = it.getString(it.getColumnIndexOrThrow("hora_inicio")),
                        deliveryEnd = it.getString(it.getColumnIndexOrThrow("hora_fin"))
                    )
                )
            }
        }
        db.close()
        return resultado
    }

    fun buscarSolicitud(id: Int): AdoptionRequest? {
        return listarSolicitudes().find { it.id == id }
    }

    private fun fechaActual(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
