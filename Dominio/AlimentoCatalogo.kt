package com.example.nutrify.Dominio

class AlimentoCatalogo(
    id: String,
    nombre: String,
    caloriasPorPorcion: Double,
    proteinasPorPorcion: Double,
    carbohidratosPorPorcion: Double,
    grasasPorPorcion: Double,
    fibraPorPorcion: Double,
    unidadPorcion: UnidadPorcion,
    val categoria: CategoriaAlimento
) : AlimentoBase(
    id = id,
    nombre = nombre,
    caloriasPorPorcion = caloriasPorPorcion,
    proteinasPorPorcion = proteinasPorPorcion,
    carbohidratosPorPorcion = carbohidratosPorPorcion,
    grasasPorPorcion = grasasPorPorcion,
    fibraPorPorcion = fibraPorPorcion,
    unidadPorcion = unidadPorcion
)
