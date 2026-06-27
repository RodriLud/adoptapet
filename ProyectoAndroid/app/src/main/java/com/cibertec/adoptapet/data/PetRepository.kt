package com.cibertec.adoptapet.data

import android.content.Context
import com.cibertec.adoptapet.database.MascotaDao
import com.cibertec.adoptapet.models.Mascota
import com.cibertec.adoptapet.models.Pet
import com.cibertec.adoptapet.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object PetRepository {

    var pets: List<Pet> = emptyList()
        private set

    fun cargarMascotas(context: Context, callback: (List<Pet>, Boolean) -> Unit) {
        ApiClient.mascotaService().listarMascotas().enqueue(object : Callback<List<Mascota>> {
            override fun onResponse(
                call: Call<List<Mascota>>,
                response: Response<List<Mascota>>
            ) {
                if (response.isSuccessful) {
                    val todas = response.body().orEmpty()
                    val mascotas = todas
                        .filter { it.estaDisponible() }
                        .map { it.toPet() }

                    // Actualizamos siempre si la respuesta fue exitosa
                    pets = mascotas
                    MascotaDao(context).guardarMascotas(mascotas)
                    callback(mascotas, true)
                } else {
                    cargarDesdeSqlite(context, callback)
                }
            }

            override fun onFailure(call: Call<List<Mascota>>, t: Throwable) {
                cargarDesdeSqlite(context, callback)
            }
        })
    }

    fun buscarMascota(context: Context, id: Int, callback: (Pet?) -> Unit) {
        pets.find { it.id == id }?.let {
            callback(it)
            return
        }

        val local = MascotaDao(context).buscarPorId(id)
        if (local != null) {
            callback(local)
            return
        }

        ApiClient.mascotaService().buscarMascota(id).enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                callback(response.body()?.toPet())
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                callback(null)
            }
        })
    }

    private fun cargarDesdeSqlite(context: Context, callback: (List<Pet>, Boolean) -> Unit) {
        val locales = MascotaDao(context).listarMascotas()
        pets = locales
        callback(locales, false)
    }
}
