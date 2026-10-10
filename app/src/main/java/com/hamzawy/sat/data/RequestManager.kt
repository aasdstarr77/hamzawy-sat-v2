package com.hamzawy.sat.data

object RequestManager {

    fun canAccept(status: TechnicianStatus): Boolean {
        return status == TechnicianStatus.APPROVED
    }

    fun canViewCustomer(status: TechnicianStatus): Boolean {
        return status == TechnicianStatus.APPROVED
    }

    fun msg(status: TechnicianStatus): String {
        return when (status) {
            TechnicianStatus.PENDING -> "قيد المراجعة"
            TechnicianStatus.SUSPENDED -> "تم تعليقك"
            TechnicianStatus.APPROVED -> "نشط"
        }
    }
}
