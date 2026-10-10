package com.hamzawy.sat.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hamzawy.sat.data.Technician
import com.hamzawy.sat.data.TechnicianStatus

@Composable
fun AdminScreen() {
    var list by remember {
        mutableStateOf(
            listOf(
                Technician("1", "احمد", "01012345678", "الخصوص", TechnicianStatus.PENDING),
                Technician("2", "محمد", "01098765432", "القليوبية", TechnicianStatus.APPROVED)
            )
        )
    }

    Column(Modifier.padding(16.dp)) {
        Text("تحكم الفنيين", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn {
            items(list) { technician ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("${technician.name} - ${technician.phone} - ${technician.status}")

                        Row {
                            Button(
                                onClick = {
                                    list = list.map {
                                        if (it.id == technician.id) {
                                            it.copy(status = TechnicianStatus.APPROVED)
                                        } else {
                                            it
                                        }
                                    }
                                }
                            ) {
                                Text("قبول")
                            }

                            Spacer(Modifier.width(6.dp))

                            Button(
                                onClick = {
                                    list = list.map {
                                        if (it.id == technician.id) {
                                            it.copy(status = TechnicianStatus.SUSPENDED)
                                        } else {
                                            it
                                        }
                                    }
                                }
                            ) {
                                Text("تعليق")
                            }

                            Spacer(Modifier.width(6.dp))

                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                onClick = {
                                    list = list.filter { it.id != technician.id }
                                }
                            ) {
                                Text("إزالة")
                            }
                        }
                    }
                }
            }
        }
    }
}
