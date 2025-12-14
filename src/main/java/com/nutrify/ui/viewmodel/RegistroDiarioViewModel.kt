package com.nutrify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.dominio.modelos.TotalesDiarios

import com.nutrify.managers.RegistroAlimentosManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.time.LocalDate

data class RegistroDiarioState(
    val fechaSeleccionada: LocalDate = LocalDate.now(),
    val registros: List<RegistroAlimento> = emptyList(),
    val totales: TotalesDiarios = TotalesDiarios(0.0, 0.0, 0.0),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val mostrarDialogoEliminar: Boolean = false,
    val registroAEliminar: RegistroAlimento? = null,
    val filtroTipoComida: String? = null,
    val tiposComidaDisponibles: List<String> = emptyList()
)

class RegistroDiarioViewModel(
    private val registroManager: RegistroAlimentosManager
) : ViewModel() {

    private val _state = MutableStateFlow(RegistroDiarioState())
    val state: StateFlow<RegistroDiarioState> = _state.asStateFlow()

    init {
        cargarRegistrosFecha(LocalDate.now())
    }

    fun cargarRegistrosFecha(fecha: LocalDate) {
        _state.update {
            it.copy(
                fechaSeleccionada = fecha,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val registros = registroManager.obtenerRegistrosPorDia(fecha)
                val totales = registroManager.obtenerTotalesDiarios(fecha)
                val tiposComida = registros.map { it.tipoComida }.distinct()

                _state.update {
                    it.copy(
                        registros = registros,
                        totales = totales,
                        tiposComidaDisponibles = tiposComida,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar registros: ${e.message}"
                    )
                }
            }
        }
    }

    fun cargarRegistrosHoy() {
        cargarRegistrosFecha(LocalDate.now())
    }

    fun cargarRegistrosAyer() {
        val ayer = LocalDate.now().minusDays(1)
        cargarRegistrosFecha(ayer)
    }

    fun cambiarFecha(fecha: LocalDate) {
        cargarRegistrosFecha(fecha)
    }

    fun mostrarDialogoEliminar(registro: RegistroAlimento) {
        _state.update {
            it.copy(
                mostrarDialogoEliminar = true,
                registroAEliminar = registro
            )
        }
    }

    fun ocultarDialogoEliminar() {
        _state.update {
            it.copy(
                mostrarDialogoEliminar = false,
                registroAEliminar = null
            )
        }
    }

    fun eliminarRegistro() {
        val registro = _state.value.registroAEliminar
        if (registro == null) {
            _state.update { it.copy(errorMessage = "No hay registro seleccionado") }
            return
        }

        viewModelScope.launch {
            try {
                val exito = registroManager.eliminarRegistro(registro.id)
                if (exito) {
                    // Recargar registros
                    cargarRegistrosFecha(_state.value.fechaSeleccionada)
                    ocultarDialogoEliminar()
                } else {
                    _state.update { it.copy(errorMessage = "Error al eliminar el registro") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Error: ${e.message}") }
            }
        }
    }

    fun aplicarFiltroTipoComida(tipoComida: String?) {
        _state.update { it.copy(filtroTipoComida = tipoComida) }
    }

    fun limpiarFiltro() {
        _state.update { it.copy(filtroTipoComida = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun obtenerRegistrosFiltrados(): List<RegistroAlimento> {
        val state = _state.value
        return if (state.filtroTipoComida != null) {
            state.registros.filter { it.tipoComida == state.filtroTipoComida }
        } else {
            state.registros
        }
    }

    fun obtenerResumenPorTipoComida(): Map<String, TotalesDiarios> {
        val state = _state.value
        val resumen = mutableMapOf<String, TotalesDiarios>()

        state.registros.groupBy { it.tipoComida }.forEach { (tipo, registros) ->
            val totales = TotalesDiarios(0.0, 0.0, 0.0)
            registros.forEach { totales.agregarRegistro(it) }
            resumen[tipo] = totales
        }

        return resumen
    }
}
