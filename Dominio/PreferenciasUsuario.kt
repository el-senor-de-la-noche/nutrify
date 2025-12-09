package com.example.nutrify.Dominio

import com.example.nutrify.Dominio.UnidadAltura
import com.example.nutrify.Dominio.UnidadPeso

class PreferenciasUsuario(
    val usuarioId: String,
    var temaOscuro: Boolean = true,
    var idioma: String = "es",
    var unidadPeso: UnidadPeso = UnidadPeso.KG,
    var unidadAltura: UnidadAltura = UnidadAltura.CENTIMETROS,
    var notificacionesActivas: Boolean = true
) : EntidadBase(id = usuarioId)
