package com.nutrify.repository

import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.services.StorageService
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDate
import java.time.LocalDateTime

class RegistroComidaMemoryDataSource(
    private val storageService: StorageService,
    private val fileName: String = "registro_comidas.json"
) : RegistroComidaDataSource {

    // ✅ Serializador para LocalDateTime (porque usas @Contextual)
    private object LocalDateTimeIsoSerializer : KSerializer<LocalDateTime> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            encoder.encodeString(value.toString()) // ISO-8601
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            return LocalDateTime.parse(decoder.decodeString())
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        explicitNulls = false
        prettyPrint = true
        classDiscriminator = "type"
        serializersModule = SerializersModule {
            contextual(LocalDateTime::class, LocalDateTimeIsoSerializer)
        }
    }

    private val registros = mutableListOf<RegistroAlimento>()

    init {
        cargarDesdeDisco()
    }

    override fun guardarRegistro(registro: RegistroAlimento): Boolean {
        return try {
            val index = registros.indexOfFirst { it.id == registro.id }
            if (index != -1) registros[index] = registro else registros.add(registro)
            persistir()
            true
        } catch (_: Exception) {
            false
        }
    }

    // ✅ FIX: filtrar por día usando toLocalDate()
    override fun obtenerRegistrosPorDia(fecha: LocalDate): List<RegistroAlimento> {
        return registros.filter { it.fecha.toLocalDate() == fecha }
    }

    override fun obtenerTodos(): List<RegistroAlimento> = registros.toList()

    override fun obtenerRegistrosPorUsuario(usuarioId: String): List<RegistroAlimento> {
        return registros.filter { it.usuarioId == usuarioId }
    }

    override fun borrarTodos(): Boolean {
        return try {
            registros.clear()
            persistir()
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun eliminarRegistro(id: String): Boolean {
        return try {
            val before = registros.size
            registros.removeAll { it.id == id }
            val changed = registros.size < before
            if (changed) persistir()
            changed
        } catch (_: Exception) {
            false
        }
    }

    private fun persistir() {
        val raw = json.encodeToString(registros)
        storageService.guardarJSON(fileName, raw)
    }

    private fun cargarDesdeDisco() {
        try {
            val raw = storageService.leerJSON(fileName)
            if (raw.isBlank() || raw == "{}") return
            val lista = json.decodeFromString<List<RegistroAlimento>>(raw)
            registros.clear()
            registros.addAll(lista)
        } catch (_: Exception) {
            // si falla, no rompe la app
        }
    }
}
