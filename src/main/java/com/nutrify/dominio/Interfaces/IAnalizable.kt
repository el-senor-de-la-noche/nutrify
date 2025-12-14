package com.nutrify.dominio.Interfaces

interface IAnalizable {
    fun calcularCalorias(): Double
    fun obtenerDescripcion(): String
    fun validar(): Boolean
}