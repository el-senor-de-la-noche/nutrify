package com.example.nutrify.Dominio

import java.util.Date

data class ResumenDiario(
    val usuarioId: String,
    val fecha: Date,
    val caloriasConsumidas: Double,
    val proteinasConsumidas: Double,
    val carbohidratosConsumidos: Double,
    val grasasConsumidas: Double,
    val fibraConsumida: Double
) {

    fun porcentajeMeta(meta: MetaNutricional): Double {
        if (meta.caloriasObjetivo <= 0) return 0.0
        return (caloriasConsumidas / meta.caloriasObjetivo) * 100.0
    }
}
