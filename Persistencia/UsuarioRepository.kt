package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.persistencia.IUsuarioDataSource

class UsuarioRepository(
    private val dataSource: IUsuarioDataSource
) {

    fun guardar(usuario: Usuario) = dataSource.guardarUsuario(usuario)

    fun obtener(): Usuario? = dataSource.obtenerUsuario()

    fun borrar() = dataSource.borrarUsuario()
}
