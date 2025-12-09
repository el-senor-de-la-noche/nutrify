package com.example.nutrify.persistencia

import com.example.nutrify.Dominio.Usuario

interface IUsuarioDataSource {
    fun guardarUsuario(usuario: Usuario)
    fun obtenerUsuario(): Usuario?
    fun borrarUsuario()
}
