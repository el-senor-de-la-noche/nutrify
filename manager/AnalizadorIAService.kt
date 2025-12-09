package com.example.nutrify.manager

import android.graphics.Bitmap
import com.example.nutrify.Dominio.AnalisisNutricional
import kotlinx.coroutines.delay

class AnalizadorIAService {

    // Simula un análisis "inteligente" sin depender de Gemini / red
    suspend fun analizarImagen(bitmap: Bitmap): AnalisisNutricional {
        // Simulamos tiempo de procesamiento
        delay(1500)

        // Aquí podrías usar ML Kit para leer texto / etiquetar imagen de verdad.
        // De momento devolvemos valores fijos de ejemplo.
        return AnalisisNutricional(
            alimentoDetectado = "Plato de comida",
            confianza = 0.85,
            caloriasEstimadas = 550.0,
            proteinasEstimadas = 30.0,
            carbohidratosEstimados = 60.0,
            grasasEstimadas = 20.0,
            fibraEstimada = 8.0,
            respuestaBrutaJSON = "{}"
        )
    }
}
