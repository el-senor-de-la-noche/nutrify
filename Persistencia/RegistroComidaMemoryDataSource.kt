package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.RegistroComida

/**
 * Implementación sencilla en memoria de IRegistroComidaDataSource.
 */
class RegistroComidaMemoryDataSource : IRegistroComidaDataSource {

    private val registros = mutableListOf<RegistroComida>()

    override fun guardarRegistros(registros: List<RegistroComida>) {
        this.registros.clear()
        this.registros.addAll(registros)
    }

    override fun cargarRegistros(): List<RegistroComida> =
        registros.toList()
}
