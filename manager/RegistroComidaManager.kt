package com.example.nutrify.manager

import com.example.nutrify.Dominio.RegistroComida
import com.example.nutrify.persistencia.RegistroComidaRepository
import java.util.Date

class RegistroComidaManager(registroRepo: RegistroComidaRepository) {

    private val registros = mutableListOf<RegistroComida>()

    fun agregarRegistro(registro: RegistroComida) {
        registros.add(registro)
    }

    fun registrosPorDia(usuarioId: String, fecha: Date): List<RegistroComida> {
        return registros.filter { it.usuarioId == usuarioId && esMismoDia(it.fechaHora, fecha) }
    }

    private fun esMismoDia(a: Date, b: Date): Boolean {
        val ca = java.util.Calendar.getInstance().apply { time = a }
        val cb = java.util.Calendar.getInstance().apply { time = b }
        return ca.get(java.util.Calendar.YEAR) == cb.get(java.util.Calendar.YEAR) &&
                ca.get(java.util.Calendar.DAY_OF_YEAR) == cb.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
