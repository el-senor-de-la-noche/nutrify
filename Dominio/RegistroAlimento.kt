package com.example.nutrify.Dominio

import java.util.Date

data class RegistroAlimento(
    val fecha: Date,
    val alimento: String,
    val calorias: Double,
    val proteinas: Double,
    val fibra: Double
)
