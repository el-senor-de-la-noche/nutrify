package com.example.nutrify.Dominio

data class AnalisisNutricional(
    val confianza: Double,
    val respuestaBrutaJSON: String,
    val alimentoDetectado: String,
    val caloriasEstimadas: Double,
    val proteinasEstimadas: Double,
    val carbohidratosEstimados: Double,
    val grasasEstimadas: Double,
    val fibraEstimada: Double,

    )
