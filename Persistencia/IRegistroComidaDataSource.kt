package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.RegistroComida

interface IRegistroComidaDataSource {
    fun guardarRegistros(registros: List<RegistroComida>)
    fun cargarRegistros(): List<RegistroComida>
}
