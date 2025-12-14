package com.nutrify.managers

import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.dominio.modelos.TotalesDiarios
import com.nutrify.repository.RegistroComidaRepository
import java.time.LocalDate
import java.util.UUID

class RegistroAlimentosManager(
    private val repo: RegistroComidaRepository,
    private val authManager: UsuarioAuthManager
) {

    fun agregarRegistro(registro: RegistroAlimento): Boolean {
        val usuario = authManager.obtenerUsuarioSesionActiva() ?: return false

        val idFinal = if (registro.id.isBlank()) UUID.randomUUID().toString() else registro.id
        val usuarioIdFinal = if (registro.usuarioId.isBlank()) usuario.id else registro.usuarioId

        // ✅ NO copy(): como id/usuarioId son var en RegistroAlimento, los seteamos directo
        registro.id = idFinal
        registro.usuarioId = usuarioIdFinal

        return repo.guardarRegistro(registro)
    }

    fun obtenerRegistrosPorDia(fecha: LocalDate): List<RegistroAlimento> {
        return repo.obtenerRegistrosPorDia(fecha)
    }

    fun obtenerTotalesDiarios(fecha: LocalDate): TotalesDiarios {
        val registros = repo.obtenerRegistrosPorDia(fecha)
        val totales = TotalesDiarios(0.0, 0.0, 0.0)
        registros.forEach { totales.agregarRegistro(it) }
        return totales
    }

    fun eliminarRegistro(id: String): Boolean = repo.eliminarRegistro(id)
}
