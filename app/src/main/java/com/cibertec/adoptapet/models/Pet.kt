package com.cibertec.adoptapet.models

data class Pet(
    val id: Int,
    val name: String,
    val type: String,
    val age: String,
    val gender: String,
    val size: String,
    val sterilized: Boolean,
    val description: String,
    val imageRes: Int
)
