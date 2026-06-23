package com.cibertec.adoptapet.models

data class AppNotification(
    val id: Long,
    val title: String,
    val body: String,
    val date: String,
    val read: Boolean = false
)
