package com.example.nutrify.Dominio

open class AnalisisIA(
    val alimentoDetectado: String,
    val confianza: Double,
    val caloriasEstimadas: Double,
    val proteinasEstimadas: Double,
    val carbohidratosEstimados: Double,
    val grasasEstimadas: Double,
    val fibraEstimada: Double,
    val respuestaBrutaJSON: String
)
