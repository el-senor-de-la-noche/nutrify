package com.example.nutrify.ui.ViewModels

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrify.Dominio.AnalisisNutricional
import com.example.nutrify.manager.AnalizadorIAService
import kotlinx.coroutines.launch

class CamaraViewModel(
    private val analizadorIA: AnalizadorIAService
) : ViewModel() {

    var cargando = mutableStateOf(false)
    var resultado = mutableStateOf<AnalisisNutricional?>(null)
    var error = mutableStateOf<String?>(null)

    fun analizarFoto(bitmap: Bitmap) {
        cargando.value = true
        viewModelScope.launch {
            try {
                val analisis = analizadorIA.analizarImagen(bitmap)
                resultado.value = analisis
                error.value = null
            } catch (e: Exception) {
                error.value = "Error al analizar la imagen"
            } finally {
                cargando.value = false
            }
        }
    }
}

