package com.example.nutrify.Dominio

import java.util.Date

abstract class EntidadBase(
    val id: String,
    val creadoEn: Date = Date(),
    var actualizadoEn: Date = Date()
)
