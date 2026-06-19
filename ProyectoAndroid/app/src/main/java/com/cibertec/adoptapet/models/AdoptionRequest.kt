package com.cibertec.adoptapet.models

data class AdoptionRequest(
    val id: Int,
    val petId: Int,
    val petName: String,
    val applicantName: String,
    val dni: String,
    val phone: String,
    val email: String,
    val address: String,
    val hasExperience: String,
    val reason: String,
    val date: String,
    var status: String,
    val rejectionReason: String? = null,
    val deliveryDate: String? = null,
    val deliveryStart: String? = null,
    val deliveryEnd: String? = null,
    val pickupLimit: String? = null,
    val scheduleStatus: String? = null,
    val comments: String? = null
)
