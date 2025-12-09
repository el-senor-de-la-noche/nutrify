package com.example.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun IMCScreen(
    imc: Double,            // se ignoran para no romper firma, pero se usan como valor inicial
    categoria: String?,
    onVolver: () -> Unit
) {
    var pesoTexto by remember { mutableStateOf("") }
    var alturaTexto by remember { mutableStateOf("") }

    var imcLocal: Float by remember { mutableStateOf(imc) }
    var categoriaLocal by remember { mutableStateOf(categoria ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "Calculadora de IMC")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pesoTexto,
            onValueChange = { pesoTexto = it },
            label = { Text("Peso (kg)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = alturaTexto,
            onValueChange = { alturaTexto = it },
            label = { Text("Altura (m, ej 1.75)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                error = null
                val peso = pesoTexto.replace(',', '.').toFloatOrNull()
                val altura = alturaTexto.replace(',', '.').toFloatOrNull()

                if (peso == null || altura == null || altura <= 0f) {
                    error = "Ingresa peso y altura válidos"
                    imcLocal = null
                    categoriaLocal = ""
                } else {
                    val valorImc = peso / (altura * altura)
                    imcLocal = valorImc

                    categoriaLocal = when {
                        valorImc < 18.5f -> "Bajo peso"
                        valorImc < 25f -> "Normopeso"
                        valorImc < 30f -> "Sobrepeso"
                        else -> "Obesidad"
                    }
                }
            }
        ) {
            Text("Calcular IMC")
        }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(text = it)
        }

        imcLocal?.let {
            Text(text = "IMC: ${"%.2f".format(it)}")
            Text(text = "Categoría: $categoriaLocal")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onVolver) {
            Text("Volver")
        }
    }
}
