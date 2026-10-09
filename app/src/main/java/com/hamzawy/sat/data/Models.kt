package com.hamzawy.sat.data

enum class TechnicianStatus { PENDING, APPROVED, SUSPENDED }

data class Technician(
    val id: String,
    val name: String,
    val phone: String,
    val area: String,
    val status: TechnicianStatus = TechnicianStatus.PENDING
)

data class AdminAction(
    val technicianId: String,
    val action: String // APPROVE, SUSPEND, REMOVE
)
