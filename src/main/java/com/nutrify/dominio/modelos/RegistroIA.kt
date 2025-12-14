package com.nutrify.dominio.modelos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Serializable
class RegistroIA private constructor(
    val analisis: AnalisisNutricional,
    val imagenHash: String? = null,
    private var _confianza: Double = 0.0
) : RegistroAlimento(
    id = "",
    usuarioId = "",
    fecha = LocalDateTime.now(),
    tipoComida = ""
) {

    constructor(
        id: String,
        usuarioId: String,
        fecha: LocalDateTime,
        tipoComida: String,
        analisis: AnalisisNutricional,
        imagenHash: String? = null,
        confianza: Double = 0.0
    ) : this(
        analisis = analisis,
        imagenHash = imagenHash,
        _confianza = confianza
    ) {
        this.id = id
        this.usuarioId = usuarioId
        this.fecha = fecha
        this.tipoComida = tipoComida
    }

    val confianza: Double get() = _confianza

    override val caloriasTotales: Double
        get() = analisis.calorias

    override val proteinasTotales: Double
        get() = analisis.proteinas

    override val fibraTotal: Double
        get() = analisis.fibra

    override fun generarResumen(): String =
        "Análisis por IA con ${(_confianza * 100).toInt()}% de confianza"

    // ✅ NO EXISTE analisis.obtenerDescripcion()
    // Usamos la propiedad descripcion del análisis si existe, o texto genérico.
    override fun obtenerDescripcion(): String =
        analisis.descripcion ?: "Comida analizada"
}
