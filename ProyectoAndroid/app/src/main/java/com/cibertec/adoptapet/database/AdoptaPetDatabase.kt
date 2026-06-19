package com.cibertec.adoptapet.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AdoptaPetDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuario (
                id_usuario INTEGER PRIMARY KEY,
                username TEXT NOT NULL,
                nombre TEXT,
                email TEXT,
                rol TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE mascotas (
                id_mascota INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                especie TEXT,
                raza TEXT,
                sexo TEXT,
                edad_texto TEXT,
                edad_anios INTEGER,
                edad_meses INTEGER,
                estado_salud TEXT,
                estado_adopcion TEXT,
                estado_esterilizacion TEXT,
                observaciones TEXT,
                ruta_imagen TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE solicitudes (
                id_solicitud INTEGER PRIMARY KEY,
                id_mascota INTEGER,
                nombre_mascota TEXT,
                fecha_registro TEXT,
                estado_solicitud TEXT,
                fecha_entrega TEXT,
                hora_inicio TEXT,
                hora_fin TEXT,
                motivo_rechazo TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE favoritos (
                id_mascota INTEGER PRIMARY KEY
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE borradores_solicitud (
                id_borrador INTEGER PRIMARY KEY AUTOINCREMENT,
                id_mascota INTEGER NOT NULL,
                motivo_adopcion TEXT,
                tipo_vivienda TEXT,
                experiencia_mascotas TEXT,
                otras_mascotas TEXT,
                cantidad_personas_hogar INTEGER,
                comentarios_adicionales TEXT,
                fecha_guardado TEXT
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE notificaciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                cuerpo TEXT NOT NULL,
                fecha TEXT NOT NULL,
                leida INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS borradores_solicitud")
            db.execSQL("DROP TABLE IF EXISTS favoritos")
            db.execSQL("DROP TABLE IF EXISTS solicitudes")
            db.execSQL("DROP TABLE IF EXISTS mascotas")
            db.execSQL("DROP TABLE IF EXISTS usuario")
            onCreate(db)
            return
        }

        if (oldVersion < 3) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notificaciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    titulo TEXT NOT NULL,
                    cuerpo TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    leida INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    companion object {
        private const val DATABASE_NAME = "adoptapet.db"
        private const val DATABASE_VERSION = 3
    }
}
