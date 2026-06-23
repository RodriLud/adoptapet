package com.cibertec.adoptapet.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object MultipartUtils {

    fun texto(valor: String): RequestBody {
        return valor.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun archivo(context: Context, nombreParte: String, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val nombreArchivo = obtenerNombreArchivo(context, uri)
        val body = bytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(nombreParte, nombreArchivo, body)
    }

    private fun obtenerNombreArchivo(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && it.moveToFirst()) {
                return it.getString(nameIndex)
            }
        }
        return "archivo_${System.currentTimeMillis()}.jpg"
    }
}
