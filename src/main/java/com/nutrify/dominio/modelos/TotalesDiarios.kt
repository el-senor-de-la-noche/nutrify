package com.nutrify.dominio.modelos

import kotlinx.serialization.Serializable

@Serializable
data class TotalesDiarios(
    private var _calorias: Double,
    private var _proteinas: Double,
    private var _fibra: Double
) {
    // Getters
    val calorias: Double get() = _calorias
    val proteinas: Double get() = _proteinas
    val fibra: Double get() = _fibra

    // Método para actualizar sumando registros
    fun agregarRegistro(registro: RegistroAlimento) {
        _calorias += registro.caloriasTotales
        _proteinas += registro.proteinasTotales
        _fibra += registro.fibraTotal
    }

    fun reiniciar() {
        _calorias = 0.0
        _proteinas = 0.0
        _fibra = 0.0
    }

    fun calcularPorcentajeObjetivo(objetivoCalorias: Double): Double {
        return if (objetivoCalorias > 0) {
            (_calorias / objetivoCalorias) * 100
        } else 0.0
    }
}