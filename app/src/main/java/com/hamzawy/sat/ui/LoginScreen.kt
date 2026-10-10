package com.hamzawy.sat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamzawy.sat.data.AuthManager
import com.hamzawy.sat.data.Technician
import com.hamzawy.sat.data.TechnicianStatus

@Composable
fun LoginScreen(
    allTechs: List<Technician>,
    onLogin: (Technician) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        TextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم التليفون") }
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val technician = allTechs.find { it.phone == phone }

                if (technician == null ||
                    AuthManager.isRemoved(technician.id, allTechs)
                ) {
                    msg = "تم إزالة حسابك"
                } else if (technician.status == TechnicianStatus.PENDING) {
                    msg = "حسابك قيد المراجعة"
                } else {
                    onLogin(technician)
                }
            }
        ) {
            Text("دخول")
        }

        Text(msg)
    }
}
