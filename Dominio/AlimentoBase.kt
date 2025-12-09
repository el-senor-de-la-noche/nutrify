package com.example.nutrify.Dominio

abstract class AlimentoBase(
    id: String,
    val nombre: String,
    val caloriasPorPorcion: Double,
    val proteinasPorPorcion: Double,
    val carbohidratosPorPorcion: Double,
    val grasasPorPorcion: Double,
    val fibraPorPorcion: Double,
    val unidadPorcion: UnidadPorcion
) : EntidadBase(id = id)
