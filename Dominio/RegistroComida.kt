package com.example.nutrify.Dominio

import java.util.Date

data class RegistroComida(
    val usuarioId: String,
    val fechaHora: Date,
    val porciones: List<PorcionAlimento>,
    val analisisIA: AnalisisIA? = null,
    val rutaFoto: String? = null,
    val notaUsuario: String? = null
) {

    fun caloriasTotales(): Double =
        porciones.sumOf { it.caloriasTotales }

    fun proteinasTotales(): Double =
        porciones.sumOf { it.proteinasTotales }

    fun fibraTotal(): Double =
        porciones.sumOf { it.fibraTotal }

    fun carbohidratosTotales(): Double =
        porciones.sumOf { it.carbohidratosTotales }

    fun grasasTotales(): Double =
        porciones.sumOf { it.grasasTotales }
}
