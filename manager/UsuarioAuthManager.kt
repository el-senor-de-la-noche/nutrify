package com.example.nutrify.manager

import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.persistencia.UsuarioRepository
import java.util.Date

class UsuarioAuthManager(usuarioRepo: UsuarioRepository) {

    private var usuarioLogeado: Usuario? = null

    fun registrar(nombre: String, email: String, password: String): Usuario {
        val user = Usuario(
            nombre = nombre,
            email = email,
            passwordHash = password.hashCode().toString(),
            fechaNacimiento = Date(),
            sexo = com.example.nutrify.Dominio.Sexo.OTRO,
            pesoActualKg = 70.0,
            alturaCm = 1.70,
            nivelActividad = com.example.nutrify.Dominio.NivelActividad.MODERADO,
            objetivo = com.example.nutrify.Dominio.ObjetivoNutricional.MANTENER_PESO
        )

        usuarioLogeado = user
        return user
    }

    fun login(email: String, password: String): Boolean {
        usuarioLogeado?.let {
            return it.email == email && it.passwordHash == password.hashCode().toString()
        }
        return false
    }

    fun logout() {
        usuarioLogeado = null
    }

    fun obtenerUsuario(): Usuario? = usuarioLogeado

    fun estaLogeado(): Boolean = usuarioLogeado != null
}
