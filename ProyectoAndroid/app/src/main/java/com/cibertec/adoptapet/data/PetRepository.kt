package com.cibertec.adoptapet.data

import com.cibertec.adoptapet.R
import com.cibertec.adoptapet.models.Pet

object PetRepository {

    val pets = listOf(
        Pet(
            id = 1,
            name = "Luna",
            type = "Perro",
            age = "2 años",
            gender = "Hembra",
            size = "Tamaño mediano",
            sterilized = true,
            description = "Luna es una perrita cariñosa, juguetona y muy leal. Le encanta correr en el parque y recibir muchos mimos.",
            imageRes = R.drawable.kitten1
        ),
        Pet(
            id = 2,
            name = "Michi",
            type = "Gato",
            age = "1 año",
            gender = "Macho",
            size = "Tamaño pequeño",
            sterilized = false,
            description = "Michi es tranquilo, curioso y muy cariñoso.",
            imageRes = R.drawable.kitten1
        ),
        Pet(
            id = 3,
            name = "Rocky",
            type = "Perro",
            age = "3 años",
            gender = "Macho",
            size = "Tamaño grande",
            sterilized = false,
            description = "Rocky es activo, protector y muy sociable.",
            imageRes = R.drawable.kitten1
        )
    )
}