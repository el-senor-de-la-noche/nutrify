package com.nutrify.dominio.modelos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime

@Serializable
class RegistroManual private constructor(
    val porciones: List<PorcionAlimento>,
    private var _observaciones: String = ""
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
        porciones: List<PorcionAlimento>,
        observaciones: String = ""
    ) : this(
        porciones = porciones,
        _observaciones = observaciones
    ) {
        this.id = id
        this.usuarioId = usuarioId
        this.fecha = fecha
        this.tipoComida = tipoComida
    }

    override val caloriasTotales: Double
        get() = porciones.sumOf { it.calorias }

    override val proteinasTotales: Double
        get() = porciones.sumOf { it.proteinas }

    override val fibraTotal: Double
        get() = porciones.sumOf { it.fibra }

    override fun generarResumen(): String =
        "Registro manual con ${porciones.size} porciones. $_observaciones"

    override fun obtenerDescripcion(): String =
        "Comida $tipoComida registrada manualmente"
}
