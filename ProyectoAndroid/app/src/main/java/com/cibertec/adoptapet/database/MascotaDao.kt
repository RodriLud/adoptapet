package com.cibertec.adoptapet.database

import android.content.ContentValues
import android.content.Context
import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.models.Pet

class MascotaDao(context: Context) {

    private val dbHelper = AdoptaPetDatabase(context)

    fun guardarMascotas(mascotas: List<Pet>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("mascotas", null, null)
            mascotas.forEach { pet ->
                val valores = ContentValues().apply {
                    put("id_mascota", pet.id)
                    put("nombre", pet.name)
                    put("especie", pet.type)
                    put("raza", pet.breed.ifBlank { pet.size })
                    put("sexo", pet.gender)
                    put("edad_texto", pet.age)
                    put("estado_salud", pet.healthStatus)
                    put("estado_adopcion", pet.adoptionStatus)
                    put("estado_esterilizacion", if (pet.sterilized) "ESTERILIZADO" else "PENDIENTE")
                    put("observaciones", pet.description)
                    put("ruta_imagen", pet.imageUrl)
                }
                db.insertWithOnConflict("mascotas", null, valores, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun listarMascotas(): List<Pet> {
        val mascotas = mutableListOf<Pet>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM mascotas WHERE estado_adopcion = 'DISPONIBLE'", null)
        cursor.use {
            while (it.moveToNext()) {
                mascotas.add(
                    Pet(
                        id = it.getInt(it.getColumnIndexOrThrow("id_mascota")),
                        name = it.getString(it.getColumnIndexOrThrow("nombre")),
                        type = it.getString(it.getColumnIndexOrThrow("especie")),
                        age = it.getString(it.getColumnIndexOrThrow("edad_texto")),
                        gender = it.getString(it.getColumnIndexOrThrow("sexo")),
                        size = it.getString(it.getColumnIndexOrThrow("raza")),
                        sterilized = it.getString(it.getColumnIndexOrThrow("estado_esterilizacion")) == "ESTERILIZADO",
                        description = it.getString(it.getColumnIndexOrThrow("observaciones")),
                        imageRes = R.drawable.kitten1,
                        imageUrl = it.getString(it.getColumnIndexOrThrow("ruta_imagen")),
                        breed = it.getString(it.getColumnIndexOrThrow("raza")),
                        healthStatus = it.getString(it.getColumnIndexOrThrow("estado_salud")),
                        adoptionStatus = it.getString(it.getColumnIndexOrThrow("estado_adopcion"))
                    )
                )
            }
        }
        db.close()
        return mascotas
    }

    fun buscarPorId(id: Int): Pet? {
        return listarMascotas().find { it.id == id }
    }
}
