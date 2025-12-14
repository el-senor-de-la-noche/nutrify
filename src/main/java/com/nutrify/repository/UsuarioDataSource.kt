// app/src/main/java/com/example/nutrify/repository/UsuarioDataSource.kt
package com.nutrify.repository

import com.nutrify.dominio.modelos.Usuario

interface UsuarioDataSource {
    fun guardarUsuario(usuario: Usuario): Boolean
    fun actualizarUsuario(usuario: Usuario): Boolean
    fun obtenerUsuario(): Usuario?  // Para usuario actual
    fun obtenerUsuarioPorEmail(email: String): Usuario?  // Para búsqueda específica
    fun guardarSesionActiva(email: String)
    fun limpiarSesionActiva()
    fun borrarUsuario()
    fun existeUsuario(email: String): Boolean

    // NUEVOS MÉTODOS PARA COMPATIBILIDAD
    fun obtenerUsuarioActual(): Usuario? = obtenerUsuario()
    fun cerrarSesion() = limpiarSesionActiva()
    fun obtenerUsuario(email: String): Usuario? = obtenerUsuarioPorEmail(email)
}