package com.example.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrify.Dominio.RegistroComida

@Composable
fun RegistroDiarioScreen(
    registros: List<RegistroComida>,
    onVolver: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("Registro Diario", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        if (registros.isEmpty()) {
            Text("No hay registros para hoy.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(registros.size) { index ->
                    val item = registros[index]

                    Card(
                        Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text("Fecha: ${item.fechaHora}")
                            Text("Calorías: ${item.caloriasTotales()}")
                            Text("Proteínas: ${item.proteinasTotales()} g")
                            Text("Fibra: ${item.fibraTotal()} g")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onVolver,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver")
        }
    }
}
