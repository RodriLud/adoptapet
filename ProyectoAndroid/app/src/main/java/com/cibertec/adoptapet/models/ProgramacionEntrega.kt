package com.cibertec.adoptapet.models

data class ProgramacionEntrega(
    val id_programacion: Int?,
    val fecha_entrega: String?,
    val hora_inicio: String?,
    val hora_fin: String?,
    val fecha_limite_recojo: String?,
    val estado_programacion: String?,
    val motivo_cancelacion: String?,
    val observacion: String?
)
