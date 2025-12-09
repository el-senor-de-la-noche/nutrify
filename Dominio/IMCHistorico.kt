package com.example.nutrify.Dominio

import java.util.Date

data class IMCHistorico(
    val usuarioId: String,
    val fechaMedicion: Date,
    val pesoKg: Double,
    val imc: Double,
    val categoria: String
)
