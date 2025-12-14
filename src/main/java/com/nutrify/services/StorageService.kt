package com.nutrify.services

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDate
import java.time.LocalDateTime

class StorageService(context: Context) {

    private val appContext = context.applicationContext

    // =========================
    // SERIALIZERS
    // =========================

    private object LocalDateSerializer : KSerializer<LocalDate> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDate) {
            encoder.encodeString(value.toString()) // YYYY-MM-DD
        }

        override fun deserialize(decoder: Decoder): LocalDate {
            return LocalDate.parse(decoder.decodeString())
        }
    }

    private object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: LocalDateTime) {
            encoder.encodeString(value.toString()) // ISO-8601
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            return LocalDateTime.parse(decoder.decodeString())
        }
    }

    private val module = SerializersModule {
        contextual(LocalDate::class, LocalDateSerializer)
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
    }

    val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = module
        classDiscriminator = "type"
    }

    companion object {
        @Volatile private var INSTANCE: StorageService? = null

        fun getInstance(context: Context): StorageService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: StorageService(context).also { INSTANCE = it }
            }
        }
    }

    fun guardarJSON(nombre: String, contenido: String): Boolean {
        return try {
            val file = File(appContext.filesDir, nombre)
            FileOutputStream(file).use { it.write(contenido.toByteArray()) }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun leerJSON(nombre: String): String {
        return try {
            val file = File(appContext.filesDir, nombre)
            if (file.exists()) {
                FileInputStream(file).use { it.readBytes().toString(Charsets.UTF_8) }
            } else {
                "{}"
            }
        } catch (e: Exception) {
            "{}"
        }
    }

    fun existeArchivo(nombre: String): Boolean = File(appContext.filesDir, nombre).exists()

    fun guardarObjetoJSON(nombre: String, objeto: Any): Boolean {
        return try {
            val jsonString = json.encodeToString(objeto)
            guardarJSON(nombre, jsonString)
        } catch (e: Exception) {
            false
        }
    }

    inline fun <reified T> leerObjetoJSON(nombre: String): T? {
        return try {
            val jsonString = leerJSON(nombre)
            if (jsonString.isBlank() || jsonString == "{}") return null
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
