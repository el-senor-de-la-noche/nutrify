package com.example.nutrify.Dominio

import com.example.nutrify.Dominio.UnidadPorcion

data class PorcionAlimento(
    val alimentoNombre: String,
    val cantidad: Double,
    val unidad: UnidadPorcion,
    val caloriasTotales: Double,
    val proteinasTotales: Double,
    val carbohidratosTotales: Double,
    val grasasTotales: Double,
    val fibraTotal: Double
)
