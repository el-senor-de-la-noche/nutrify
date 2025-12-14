package com.nutrify.repository

import com.nutrify.dominio.modelos.RegistroAlimento
import java.time.LocalDate


interface RegistroComidaDataSource {
     fun guardarRegistro(registro: RegistroAlimento): Boolean
     fun obtenerRegistrosPorDia(fecha: LocalDate): List<RegistroAlimento>
     fun obtenerTodos(): List<RegistroAlimento>
     fun obtenerRegistrosPorUsuario(usuarioId: String): List<RegistroAlimento>
     fun borrarTodos(): Boolean
     fun eliminarRegistro(id: String): Boolean
}