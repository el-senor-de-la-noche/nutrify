package com.nutrify.dominio.modelos

import com.nutrify.dominio.Interfaces.IAnalizable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Serializable
sealed class RegistroAlimento(
    open var id: String,
    open var usuarioId: String,
    @Contextual open var fecha: LocalDateTime,
    open var tipoComida: String
) : IAnalizable {

    abstract val caloriasTotales: Double
    abstract val proteinasTotales: Double
    abstract val fibraTotal: Double

    abstract fun generarResumen(): String

    override fun calcularCalorias(): Double = caloriasTotales

    override fun validar(): Boolean =
        caloriasTotales >= 0 && proteinasTotales >= 0 && fibraTotal >= 0

    fun obtenerInformacionCompleta(): String =
        """
        Tipo: $tipoComida
        Fecha: $fecha
        Calorías: ${caloriasTotales} kcal
        Proteínas: ${proteinasTotales} g
        Fibra: ${fibraTotal} g
        ${generarResumen()}
        """.trimIndent()
}
