package com.nutrify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.managers.UsuarioProfileManager
import com.nutrify.services.NutricionCalculatorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class IMCState(
    val peso: String = "",
    val altura: String = "",
    val imcCalculado: Double = 0.0,
    val categoriaIMC: String = "",
    val riesgoSalud: String = "",
    val pesoIdealMin: Double = 0.0,
    val pesoIdealMax: Double = 0.0,
    val diferenciaPeso: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val usarDatosPerfil: Boolean = true,
    val usuario: Usuario? = null,
    val historialIMC: List<Pair<String, Double>> = emptyList()
)

class IMCViewModel(
    private val profileManager: UsuarioProfileManager,
    private val calculator: NutricionCalculatorService
) : ViewModel() {

    private val _state = MutableStateFlow(IMCState())
    val state: StateFlow<IMCState> = _state.asStateFlow()

    init {
        cargarDatosPerfil()
    }

    fun cargarDatosPerfil() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val usuario = profileManager.obtenerUsuario()

                _state.update {
                    it.copy(
                        usuario = usuario,
                        peso = if (it.usarDatosPerfil) usuario.peso.toString() else it.peso,
                        altura = if (it.usarDatosPerfil) usuario.altura.toString() else it.altura,
                        isLoading = false
                    )
                }
                calcularIMC()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar perfil: ${e.message}"
                    )
                }
            }
        }
    }

    fun onPesoChanged(peso: String) {
        _state.update { it.copy(peso = peso, usarDatosPerfil = false) }
    }

    fun onAlturaChanged(altura: String) {
        _state.update { it.copy(altura = altura, usarDatosPerfil = false) }
    }

    fun toggleUsarDatosPerfil() {
        val nuevoEstado = !_state.value.usarDatosPerfil
        _state.update { it.copy(usarDatosPerfil = nuevoEstado) }
        if (nuevoEstado) cargarDatosPerfil()
    }

    fun calcularIMC() {
        val state = _state.value

        val peso = state.peso.replace(",", ".").toDoubleOrNull()
        val altura = state.altura.replace(",", ".").toDoubleOrNull()

        if (peso == null || altura == null || altura <= 0) {
            _state.update { it.copy(errorMessage = "Ingrese un peso y altura válidos") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val usuarioTemp = (state.usuario?.copy()
                    ?: Usuario(
                        id = "temp",
                        nombre = "Temp",
                        email = "temp@email.com",
                        passwordHash = "",
                        fechaNacimiento = LocalDate.of(2000, 1, 1),
                        sexo = Sexo.OTRO,
                        peso = peso,
                        altura = altura,
                        nivelActividad = NivelActividad.SEDENTARIO,
                        objetivo = ObjetivoNutricional.MANTENER_PESO,
                        esInvitado = true
                    )
                        ).apply {
                        actualizarPeso(peso)
                        actualizarAltura(altura)
                    }

                val resultadoIMC = calculator.calcularIMC(usuarioTemp)

                val imc = resultadoIMC["imc"] as? Double ?: 0.0
                val categoria = resultadoIMC["categoria"] as? String ?: ""
                val riesgo = resultadoIMC["riesgo"] as? String ?: ""
                val pesoIdealMin = resultadoIMC["peso_ideal_min"] as? Double ?: 0.0
                val pesoIdealMax = resultadoIMC["peso_ideal_max"] as? Double ?: 0.0

                val diferenciaPeso = when {
                    peso > pesoIdealMax -> peso - pesoIdealMax
                    peso < pesoIdealMin -> pesoIdealMin - peso
                    else -> 0.0
                }

                val fechaActual = LocalDate.now().toString()
                val nuevoHistorial = (_state.value.historialIMC + (fechaActual to imc)).takeLast(10)

                _state.update {
                    it.copy(
                        imcCalculado = imc,
                        categoriaIMC = categoria,
                        riesgoSalud = riesgo,
                        pesoIdealMin = pesoIdealMin,
                        pesoIdealMax = pesoIdealMax,
                        diferenciaPeso = diferenciaPeso,
                        isLoading = false,
                        historialIMC = nuevoHistorial
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al calcular IMC: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun reiniciarCalculo() {
        _state.update {
            IMCState(
                usarDatosPerfil = it.usarDatosPerfil,
                usuario = it.usuario,
                historialIMC = it.historialIMC
            )
        }
    }
}
