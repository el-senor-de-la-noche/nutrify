package com.example.nutrify.Dominio

class MetaNutricional(
    val usuarioId: String,
    var caloriasObjetivo: Double,
    var proteinasObjetivo: Double,
    var carbohidratosObjetivo: Double,
    var grasasObjetivo: Double,
    var fibraObjetivo: Double
) : EntidadBase(id = usuarioId) {

    fun cumpleCalorias(consumidas: Double): Boolean =
        consumidas <= caloriasObjetivo

    fun cumpleProteinas(consumidas: Double): Boolean =
        consumidas >= proteinasObjetivo
}
