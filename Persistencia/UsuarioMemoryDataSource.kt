package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.Usuario

/**
 * Implementación sencilla en memoria de IUsuarioDataSource.
 * No guarda nada en disco, solo mientras la app está viva.
 */
class UsuarioMemoryDataSource : IUsuarioDataSource {

    private var usuario: Usuario? = null

    override fun guardarUsuario(usuario: Usuario) {
        this.usuario = usuario
    }

    override fun obtenerUsuario(): Usuario? = usuario

    override fun borrarUsuario() {
        usuario = null
    }
}
