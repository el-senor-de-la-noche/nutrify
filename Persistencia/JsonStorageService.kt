package com.example.nutrify.persistencia

import android.content.Context
import java.io.File

class JsonStorageService(private val context: Context) : IAppStorage {

    override fun escribirArchivo(nombre: String, contenido: String) {
        val archivo = File(context.filesDir, nombre)
        archivo.writeText(contenido)
    }

    override fun leerArchivo(nombre: String): String {
        val archivo = File(context.filesDir, nombre)
        return if (archivo.exists()) archivo.readText() else ""
    }

    override fun existeArchivo(nombre: String): Boolean {
        val archivo = File(context.filesDir, nombre)
        return archivo.exists()
    }
}
