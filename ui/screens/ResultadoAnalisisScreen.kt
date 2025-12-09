package com.example.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrify.Dominio.AnalisisNutricional

@Composable
fun ResultadoAnalisisScreen(
    resultado: AnalisisNutricional?,
    error: String?,
    cargando: Boolean,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Resultado de Análisis", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        when {
            cargando -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Analizando imagen...")
            }

            error != null -> {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            resultado == null -> {
                Text("No hay resultado disponible.")
            }

            else -> {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Alimento: ${resultado.alimentoDetectado}")
                        Text("Confianza: ${"%.2f".format(resultado.confianza * 100)}%")
                        Text("Calorías: ${resultado.caloriasEstimadas}")
                        Text("Proteínas: ${resultado.proteinasEstimadas} g")
                        Text("Carbohidratos: ${resultado.carbohidratosEstimados} g")
                        Text("Grasas: ${resultado.grasasEstimadas} g")
                        Text("Fibra: ${resultado.fibraEstimada} g")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
                    Text("Agregar al Registro Diario")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onVolver) {
            Text("Volver")
        }
    }
}
