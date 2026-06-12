package com.cibertec.adoptapet.data

import com.cibertec.adoptapet.models.Pet

object FavoritesRepository {

    private val idsFavoritos = mutableSetOf<Int>()

    fun obtenerFavoritos(): List<Pet> {
        return PetRepository.pets.filter { pet ->
            idsFavoritos.contains(pet.id)
        }
    }

    fun esFavorito(petId: Int): Boolean {
        return idsFavoritos.contains(petId)
    }

    fun cambiarFavorito(petId: Int) {
        if (idsFavoritos.contains(petId)) {
            idsFavoritos.remove(petId)
        } else {
            idsFavoritos.add(petId)
        }
    }
}