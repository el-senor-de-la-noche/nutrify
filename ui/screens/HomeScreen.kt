package com.example.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrify.Dominio.RegistroComida

@Composable
fun HomeScreen(
    registrosDeHoy: List<RegistroComida>,
    onOpenCamera: () -> Unit,
    onOpenRegistro: () -> Unit,
    onOpenPerfil: () -> Unit,
    onOpenIMC: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Nutrify", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(24.dp))

        if (registrosDeHoy.isNotEmpty()) {
            val totalCalorias = registrosDeHoy.sumOf { it.caloriasTotales() }
            Text("Calorías de hoy: ${"%.1f".format(totalCalorias)}")
        } else {
            Text("Aún no registras comidas hoy.")
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onOpenCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Analizar comida con la cámara")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenRegistro,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver registro diario")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenPerfil,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Perfil")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onOpenIMC,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver IMC")
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}
