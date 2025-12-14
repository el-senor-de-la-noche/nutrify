package com.nutrify.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.modelos.AnalisisNutricional
import com.nutrify.dominio.modelos.RegistroIA
import com.nutrify.managers.RegistroAlimentosManager
import com.nutrify.services.AnalizadorIAService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

data class CamaraState(
    val imagenCapturada: Bitmap? = null,
    val estaProcesando: Boolean = false,
    val analisis: AnalisisNutricional? = null,
    val mostrarResultados: Boolean = false,
    val errorMessage: String? = null
)

class CamaraViewModel(
    private val analizadorIAService: AnalizadorIAService,
    private val registroManager: RegistroAlimentosManager
) : ViewModel() {

    private val _state = MutableStateFlow(CamaraState())
    val state: StateFlow<CamaraState> = _state

    fun onImagenCapturada(bitmap: Bitmap) {
        _state.update {
            it.copy(
                imagenCapturada = bitmap,
                analisis = null,
                mostrarResultados = false,
                errorMessage = null
            )
        }
    }

    fun analizarImagen() {
        val bitmap = _state.value.imagenCapturada ?: return
        _state.update { it.copy(estaProcesando = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                val resultado = analizadorIAService.analizarImagen(imageBytes)

                _state.update {
                    it.copy(
                        estaProcesando = false,
                        analisis = resultado,
                        mostrarResultados = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        estaProcesando = false,
                        errorMessage = "Gemini falló: ${e.message ?: "error desconocido"}"
                    )
                }
            }
        }
    }

    fun guardarAnalisisEnRegistro(tipoComida: String = "Comida"): Boolean {
        val analisis = _state.value.analisis ?: return false

        val registro = RegistroIA(
            id = "",
            usuarioId = "",
            fecha = LocalDateTime.now(),
            tipoComida = tipoComida,
            analisis = analisis,
            imagenHash = null,
            confianza = 0.0
        )

        val ok = registroManager.agregarRegistro(registro)
        if (!ok) {
            _state.update { it.copy(errorMessage = "No se pudo guardar el registro") }
        }
        return ok
    }

    fun reiniciar() {
        _state.update { CamaraState() }
    }

    fun limpiarError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
