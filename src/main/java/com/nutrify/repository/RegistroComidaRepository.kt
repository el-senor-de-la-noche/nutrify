package com.nutrify.repository

import com.nutrify.dominio.modelos.RegistroAlimento
import java.time.LocalDate

class RegistroComidaRepository(
    private val localDataSource: RegistroComidaDataSource
) {
     fun guardarRegistro(registro: RegistroAlimento): Boolean {
        return localDataSource.guardarRegistro(registro)
    }

     fun obtenerRegistrosPorDia(fecha: LocalDate): List<RegistroAlimento> {
        return localDataSource.obtenerRegistrosPorDia(fecha)
    }

     fun obtenerRegistrosPorUsuario(usuarioId: String): List<RegistroAlimento> {
        return localDataSource.obtenerRegistrosPorUsuario(usuarioId)
    }

     fun obtenerTodos(): List<RegistroAlimento> {
        return localDataSource.obtenerTodos()
    }

     fun eliminarRegistro(id: String): Boolean {
        return localDataSource.eliminarRegistro(id)
    }

     fun borrarTodos(): Boolean {
        return localDataSource.borrarTodos()
    }

    // Métodos adicionales que combinan operaciones
     fun guardarMultiplesRegistros(registros: List<RegistroAlimento>): Boolean {
        var todosExitosos = true
        for (registro in registros) {
            if (!guardarRegistro(registro)) {
                todosExitosos = false
            }
        }
        return todosExitosos
    }

     fun obtenerUltimosRegistros(limite: Int): List<RegistroAlimento> {
        val todos = obtenerTodos()
        return todos.sortedByDescending { it.fecha }.take(limite)
    }

     fun obtenerRegistrosPorTipoComida(tipoComida: String): List<RegistroAlimento> {
        val todos = obtenerTodos()
        return todos.filter { it.tipoComida.equals(tipoComida, ignoreCase = true) }
    }

     fun obtenerEstadisticasPorUsuario(usuarioId: String): Map<String, Any> {
        val registros = obtenerRegistrosPorUsuario(usuarioId)

        if (registros.isEmpty()) {
            return emptyMap()
        }

        val totalCalorias = registros.sumOf { it.caloriasTotales }
        val totalProteinas = registros.sumOf { it.proteinasTotales }
        val totalFibra = registros.sumOf { it.fibraTotal }

        val fechasDistintas = registros.map { it.fecha }.distinct().size

        return mapOf(
            "total_registros" to registros.size,
            "total_calorias" to totalCalorias,
            "total_proteinas" to totalProteinas,
            "total_fibra" to totalFibra,
            "dias_con_registros" to fechasDistintas,
            "promedio_calorias_diario" to totalCalorias / fechasDistintas
        )
    }
}
