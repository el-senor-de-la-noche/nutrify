package com.example.nutrify.manager

import com.example.nutrify.Dominio.ObjetivoNutricional
import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.persistencia.UsuarioRepository

class UsuarioProfileManager(
    private val usuarioRepo: UsuarioRepository
) {

    /**
     * Devuelve el usuario actualmente guardado (si existe)
     */
    fun obtenerUsuario(): Usuario? {
        return usuarioRepo.obtener()
    }

    /**
     * Actualiza solo el peso del usuario y lo guarda en el repositorio.
     */
    fun actualizarPeso(nuevoPeso: Double) {
        val usuario = usuarioRepo.obtener() ?: return
        // Suponiendo que pesoActualKg es var en Usuario
        usuario.pesoActualKg = nuevoPeso
        usuarioRepo.guardar(usuario)
    }

    /**
     * Actualiza solo la altura del usuario y la guarda.
     */
    fun actualizarAltura(nuevaAltura: Double) {
        val usuario = usuarioRepo.obtener() ?: return
        // Suponiendo que alturaM es var en Usuario
        usuario.alturaCm = nuevaAltura
        usuarioRepo.guardar(usuario)
    }

    /**
     * Actualiza el objetivo nutricional del usuario.
     */
    fun actualizarObjetivo(objetivo: ObjetivoNutricional) {
        val usuario = usuarioRepo.obtener() ?: return
        // Suponiendo que objetivo es var en Usuario
        usuario.objetivo = objetivo
        usuarioRepo.guardar(usuario)
    }
}
