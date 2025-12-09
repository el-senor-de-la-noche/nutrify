package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.RegistroComida

class RegistroComidaRepository(
    private val dataSource: IRegistroComidaDataSource
) {

    fun guardarTodos(registros: List<RegistroComida>) =
        dataSource.guardarRegistros(registros)

    fun obtenerTodos(): List<RegistroComida> =
        dataSource.cargarRegistros()
}
