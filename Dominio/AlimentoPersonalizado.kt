package com.example.nutrify.Dominio

class AlimentoPersonalizado(
    usuarioId: String,
    nombre: String,
    caloriasPorPorcion: Double,
    proteinasPorPorcion: Double,
    carbohidratosPorPorcion: Double,
    grasasPorPorcion: Double,
    fibraPorPorcion: Double,
    unidadPorcion: UnidadPorcion,
    val notaUsuario: String? = null
) : AlimentoBase(
    id = "${usuarioId}_$nombre",
    nombre = nombre,
    caloriasPorPorcion = caloriasPorPorcion,
    proteinasPorPorcion = proteinasPorPorcion,
    carbohidratosPorPorcion = carbohidratosPorPorcion,
    grasasPorPorcion = grasasPorPorcion,
    fibraPorPorcion = fibraPorPorcion,
    unidadPorcion = unidadPorcion
)
