package com.cibertec.adoptapet.data

import android.content.Context
import com.cibertec.adoptapet.database.AdoptaPetDatabase
import com.cibertec.adoptapet.database.NotificationDao
import com.cibertec.adoptapet.models.AppNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationRepository {

    private fun dao(context: Context): NotificationDao {
        val db = AdoptaPetDatabase(context).writableDatabase
        return NotificationDao(db)
    }

    fun guardar(context: Context, titulo: String, cuerpo: String) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        dao(context).insertar(titulo, cuerpo, fecha)
    }

    fun listar(context: Context): List<AppNotification> {
        return dao(context).listarTodas()
    }

    fun marcarLeida(context: Context, id: Long) {
        dao(context).marcarLeida(id)
    }
}
