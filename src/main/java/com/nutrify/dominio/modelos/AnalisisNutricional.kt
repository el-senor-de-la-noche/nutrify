package com.nutrify.dominio.modelos

import com.nutrify.dominio.Interfaces.IAnalizable
import kotlinx.serialization.Serializable

@Serializable
data class AnalisisNutricional(
    val descripcion: String? = null,
    val calorias: Double = 0.0,
    val proteinas: Double = 0.0,
    val fibra: Double = 0.0,
    val carbohidratos: Double? = null,
    val grasas: Double? = null,
    val confianza: Double? = null,
    val fuente: String? = null
)