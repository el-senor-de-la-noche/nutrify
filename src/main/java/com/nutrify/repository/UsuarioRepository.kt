// app/src/main/java/com/example/nutrify/repository/UsuarioRepository.kt
package com.nutrify.repository

import com.nutrify.dominio.modelos.Usuario

class UsuarioRepository(
    private val localDataSource: UsuarioDataSource
) {
    fun guardarUsuario(usuario: Usuario): Boolean {
        return localDataSource.guardarUsuario(usuario)
    }

    fun actualizarUsuario(usuario: Usuario): Boolean {
        return localDataSource.actualizarUsuario(usuario)
    }

    // Método para obtener usuario actual (sin parámetros)
    fun obtenerUsuario(): Usuario? {
        return localDataSource.obtenerUsuario()
    }

    // Método para obtener usuario por email
    fun obtenerUsuarioPorEmail(email: String): Usuario? {
        return localDataSource.obtenerUsuarioPorEmail(email)
    }

    // NUEVO: Método para compatibilidad con authManager
    fun obtenerUsuarioActual(): Usuario? {
        return localDataSource.obtenerUsuario()
    }

    fun guardarSesionActiva(email: String) {
        localDataSource.guardarSesionActiva(email)
    }

    fun limpiarSesionActiva() {
        localDataSource.limpiarSesionActiva()
    }

    // NUEVO: Método para compatibilidad
    fun cerrarSesion() {
        localDataSource.limpiarSesionActiva()
    }

    fun borrarUsuario() {
        localDataSource.borrarUsuario()
    }

    fun existeUsuario(email: String): Boolean {
        return localDataSource.existeUsuario(email)
    }

    fun iniciarSesion(email: String, password: String): Usuario? {
        val usuario = obtenerUsuarioPorEmail(email)
        return if (usuario != null && usuario.verificarPassword(password)) {
            guardarSesionActiva(email)
            usuario
        } else {
            null
        }
    }

    fun estaSesionActiva(): Boolean {
        return obtenerUsuario() != null
    }

    fun cambiarPassword(email: String, nuevaPassword: String): Boolean {
        val usuario = obtenerUsuarioPorEmail(email)
        // Como la clase Usuario no tiene método para cambiar password,
        // necesitaríamos crear un nuevo usuario con el nuevo hash
        // Por simplicidad, devolvemos false por ahora
        return false
    }

    fun obtenerDatosPerfil(email: String): Map<String, Any>? {
        val usuario = obtenerUsuarioPorEmail(email)
        return usuario?.let {
            mapOf(
                "nombre" to it.nombre,
                "email" to it.email,
                "peso" to it.peso,
                "altura" to it.altura,
                "objetivo" to it.objetivo,
                "nivelActividad" to it.nivelActividad
            )
        }
    }
}
