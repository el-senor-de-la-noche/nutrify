package com.nutrify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.dominio.modelos.TotalesDiarios
import com.nutrify.managers.RegistroAlimentosManager
import com.nutrify.managers.UsuarioAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val registrosHoy: List<RegistroAlimento> = emptyList(),
    val totalesHoy: TotalesDiarios = TotalesDiarios(0.0, 0.0, 0.0),
    val objetivoCalorias: Double = 2000.0,
    val porcentajeCalorias: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userName: String = "",
    val mostrarResumen: Boolean = true
)

class HomeViewModel(
    private val registroManager: RegistroAlimentosManager,
    private val authManager: UsuarioAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()




    init {
        viewModelScope.launch {
            cargarUsuarioActual()
        }
        cargarDatosIniciales()
    }

     fun cargarUsuarioActual() {
            val usuario = authManager.obtenerUsuarioActual()
            usuario?.let {
                _state.update { state ->
                    state.copy(userName = it.nombre.split(" ").firstOrNull() ?: it.nombre)
                }
            }
        }
        fun cargarDatosIniciales() {
            cargarRegistrosHoy()
        }

        fun cargarRegistrosHoy() {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            viewModelScope.launch {
                try {
                    val fechaHoy = LocalDate.now()
                    val registros = registroManager.obtenerRegistrosPorDia(fechaHoy)
                    val totales = registroManager.obtenerTotalesDiarios(fechaHoy)

                    // Calcular objetivo de calorías (simplificado)
                    val usuario = authManager.obtenerUsuarioActual()
                    val objetivoCalorias = usuario?.let {
                        // En una implementación real, usaríamos NutricionCalculatorService
                        2000.0 // Valor por defecto
                    } ?: 2000.0

                    val porcentajeCalorias = (totales.calorias / objetivoCalorias) * 100

                    _state.update {
                        it.copy(
                            registrosHoy = registros,
                            totalesHoy = totales,
                            objetivoCalorias = objetivoCalorias,
                            porcentajeCalorias = porcentajeCalorias.coerceIn(0.0, 100.0),
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Error al cargar datos: ${e.message}"
                        )
                    }
                }
            }
        }

        fun eliminarRegistro(id: String) {
            viewModelScope.launch {
                try {
                    val exito = registroManager.eliminarRegistro(id)
                    if (exito) {
                        cargarRegistrosHoy()
                    } else {
                        _state.update {
                            it.copy(errorMessage = "Error al eliminar el registro")
                        }
                    }
                } catch (e: Exception) {
                    _state.update {
                        it.copy(errorMessage = "Error: ${e.message}")
                    }
                }
            }
        }

        fun toggleMostrarResumen() {
            _state.update { it.copy(mostrarResumen = !it.mostrarResumen) }
        }

        fun clearError() {
            _state.update { it.copy(errorMessage = null) }
        }

        fun obtenerEstadisticasRapidas(): Map<String, Any> {
            val state = _state.value
            return mapOf(
                "registros_hoy" to state.registrosHoy.size,
                "calorias_consumidas" to state.totalesHoy.calorias,
                "calorias_restantes" to (state.objetivoCalorias - state.totalesHoy.calorias).coerceAtLeast(
                    0.0
                ),
                "proteinas_consumidas" to state.totalesHoy.proteinas,
                "fibra_consumida" to state.totalesHoy.fibra
            )
        }
    }
