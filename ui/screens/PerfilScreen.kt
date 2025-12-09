package com.example.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutrify.Dominio.ObjetivoNutricional
import com.example.nutrify.Dominio.Usuario

@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onActualizarPeso: (Double) -> Unit,
    onActualizarAltura: (Double) -> Unit,
    onActualizarObjetivo: (ObjetivoNutricional) -> Unit,
    onVolver: () -> Unit
) {
    if (usuario == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Text("Cargando perfil...", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onVolver) { Text("Volver") }
        }
        return
    }

    var peso by remember { mutableStateOf(usuario.pesoActualKg.toString()) }
    var altura by remember { mutableStateOf(usuario.alturaCm.toString()) }
    var objetivo by remember { mutableStateOf(usuario.objetivo) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = altura,
            onValueChange = { altura = it },
            label = { Text("Altura (m)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Objetivo")
        Spacer(Modifier.height(8.dp))

        DropdownMenuWrapper(
            selected = objetivo,
            onSelect = { objetivo = it }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onActualizarPeso(peso.toDoubleOrNull() ?: usuario.pesoActualKg)
                onActualizarAltura(altura.toDoubleOrNull() ?: usuario.alturaCm)
                onActualizarObjetivo(objetivo)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onVolver) {
            Text("Volver")
        }
    }
}

@Composable
fun DropdownMenuWrapper(
    selected: ObjetivoNutricional,
    onSelect: (ObjetivoNutricional) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selected.name.lowercase().replaceFirstChar { it.uppercase() })
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ObjetivoNutricional.values().forEach { obj ->
                DropdownMenuItem(
                    text = { Text(obj.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelect(obj)
                        expanded = false
                    }
                )
            }
        }
    }
}
