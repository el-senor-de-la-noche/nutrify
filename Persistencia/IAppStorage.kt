package com.example.nutrify.persistencia

interface IAppStorage {
    fun escribirArchivo(nombre: String, contenido: String)
    fun leerArchivo(nombre: String): String
    fun existeArchivo(nombre: String): Boolean
}
