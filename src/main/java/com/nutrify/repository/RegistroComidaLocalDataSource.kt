package com.nutrify.repository

import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.services.StorageService
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.time.LocalDate

class RegistroComidaLocalDataSource(
    private val storageService: StorageService,
    private val fileName: String = "registros_comida.json"
) : RegistroComidaDataSource {

    // ✅ USA el Json del StorageService (con LocalDateTime + discriminator)
    private val json = storageService.json

    override fun guardarRegistro(registro: RegistroAlimento): Boolean {
        return try {
            val registrosActuales = obtenerTodos().toMutableList()

            val index = registrosActuales.indexOfFirst { it.id == registro.id }
            if (index != -1) registrosActuales[index] = registro else registrosActuales.add(registro)

            val jsonString = json.encodeToString(
                ListSerializer(RegistroAlimento.serializer()),
                registrosActuales
            )
            storageService.guardarJSON(fileName, jsonString)
        } catch (e: Exception) {
            false
        }
    }

    override fun obtenerRegistrosPorDia(fecha: LocalDate): List<RegistroAlimento> {
        return try {
            obtenerTodos().filter { it.fecha.toLocalDate() == fecha }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun obtenerTodos(): List<RegistroAlimento> {
        return try {
            val jsonString = storageService.leerJSON(fileName)
            if (jsonString.isBlank() || jsonString == "{}") return emptyList()

            json.decodeFromString(
                ListSerializer(RegistroAlimento.serializer()),
                jsonString
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun obtenerRegistrosPorUsuario(usuarioId: String): List<RegistroAlimento> {
        return try {
            obtenerTodos().filter { it.usuarioId == usuarioId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun borrarTodos(): Boolean {
        return try {
            storageService.guardarJSON(fileName, "[]")
        } catch (e: Exception) {
            false
        }
    }

    override fun eliminarRegistro(id: String): Boolean {
        return try {
            val actuales = obtenerTodos()
            val filtrados = actuales.filterNot { it.id == id }
            if (filtrados.size == actuales.size) return false

            val jsonString = json.encodeToString(
                ListSerializer(RegistroAlimento.serializer()),
                filtrados
            )
            storageService.guardarJSON(fileName, jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }
}
