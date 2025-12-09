package com.example.nutrify.Dominio

class ConfiguracionPrivacidad(
    val usuarioId: String,
    var compartirDatosAnonimos: Boolean = false,
    var permitirCopiasSeguridadNube: Boolean = false,
    var mostrarRecordatoriosSalud: Boolean = true
) : EntidadBase(id = usuarioId)
