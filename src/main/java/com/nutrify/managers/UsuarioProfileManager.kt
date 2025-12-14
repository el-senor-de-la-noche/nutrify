package com.nutrify.managers

import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.repository.UsuarioRepository
import java.time.LocalDate

class UsuarioProfileManager(
    private val persistencia: UsuarioRepository,
    private val authManager: UsuarioAuthManager
) {

    fun obtenerUsuario(): Usuario {
        return authManager.obtenerUsuarioActual()
            ?: throw IllegalStateException("Usuario no autenticado")
    }

    fun actualizarPerfilCompleto(
        nombre: String,
        fechaNacimiento: LocalDate?,
        sexo: Sexo,
        peso: Double,
        altura: Double,
        nivelActividad: NivelActividad,
        objetivo: ObjetivoNutricional
    ): Boolean {
        return try {
            val usuario = obtenerUsuario()

            usuario.actualizarNombre(nombre)
            usuario.actualizarFechaNacimiento(fechaNacimiento) // acepta null
            usuario.actualizarSexo(sexo)
            usuario.actualizarPeso(peso)
            usuario.actualizarAltura(altura)
            usuario.actualizarNivelActividad(nivelActividad)
            usuario.actualizarObjetivo(objetivo)

            // ✅ SIEMPRE persistir, incluso invitado (según tu requerimiento)
            persistencia.guardarUsuario(usuario)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun actualizarPeso(nuevoPeso: Double): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarPeso(nuevoPeso)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarAltura(nuevaAltura: Double): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarAltura(nuevaAltura)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarObjetivo(objetivo: ObjetivoNutricional): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarObjetivo(objetivo)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarNivelActividad(nivelActividad: NivelActividad): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarNivelActividad(nivelActividad)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarNombre(nuevoNombre: String): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarNombre(nuevoNombre)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarFechaNacimiento(fecha: LocalDate?): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarFechaNacimiento(fecha)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun actualizarSexo(sexo: Sexo): Boolean = try {
        val usuario = obtenerUsuario()
        usuario.actualizarSexo(sexo)
        persistencia.guardarUsuario(usuario)
        true
    } catch (e: Exception) { false }

    fun obtenerDatosPerfil(): Map<String, Any?> {
        val usuario = obtenerUsuario()
        return mapOf(
            "nombre" to usuario.nombre,
            "email" to usuario.email,
            "peso" to usuario.peso,
            "altura" to usuario.altura,
            "objetivo" to usuario.objetivo,
            "nivelActividad" to usuario.nivelActividad,
            "edad" to calcularEdad(usuario.fechaNacimiento)
        )
    }

    private fun calcularEdad(fechaNacimiento: LocalDate?): Int? {
        if (fechaNacimiento == null) return null
        val hoy = LocalDate.now()
        var edad = hoy.year - fechaNacimiento.year
        if (hoy.dayOfYear < fechaNacimiento.dayOfYear) edad--
        return edad
    }

    fun cerrarSesion() {
        authManager.logout()
    }
}
