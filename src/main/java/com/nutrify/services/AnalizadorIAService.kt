package com.nutrify.services

import android.util.Base64
import com.nutrify.dominio.modelos.AnalisisNutricional
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.Closeable

class AnalizadorIAService(
    private val apiKey: String,
    private val model: String = "gemini-2.5-flash"
) : Closeable {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
        install(Logging) { level = LogLevel.ALL } // mira Logcat para ver el error real si falla
    }

    suspend fun analizarImagen(imageBytes: ByteArray): AnalisisNutricional {
        val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val prompt = """
Devuelve SOLO un JSON válido (sin markdown) con estas llaves:
{
 "descripcion": string,
 "calorias": number,
 "proteinas": number,
 "fibra": number,
 "carbohidratos": number,
 "grasas": number,
 "confianza": number
}
Si no puedes inferir bien, igual devuelve estimación pero confianza <= 0.4.
""".trim()

        val req = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val response: GenerateContentResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }.body()

        val text = response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()
            ?: throw IllegalStateException("Gemini no devolvió texto. candidates vacío.")

        val jsonObject = extraerJson(text)
            ?: throw IllegalStateException("Gemini devolvió algo que no es JSON: $text")

        return json.decodeFromString(AnalisisNutricional.serializer(), jsonObject)
    }

    private fun extraerJson(s: String): String? {
        val start = s.indexOf('{')
        val end = s.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) return null
        return s.substring(start, end + 1)
    }

    override fun close() {
        client.close()
    }

    // =========================
    // DTO Gemini REST
    // =========================

    @Serializable
    private data class GenerateContentRequest(
        val contents: List<Content>,
        @SerialName("generationConfig") val generationConfig: GenerationConfig? = null
    )

    @Serializable
    private data class GenerationConfig(
        @SerialName("responseMimeType") val responseMimeType: String? = null,
        val temperature: Double? = null
    )

    @Serializable
    private data class Content(
        val parts: List<Part>,
        val role: String? = "user"
    )

    @Serializable
    private data class Part(
        val text: String? = null,
        @SerialName("inline_data") val inlineData: InlineData? = null
    )

    @Serializable
    private data class InlineData(
        @SerialName("mime_type") val mimeType: String,
        val data: String
    )

    @Serializable
    private data class GenerateContentResponse(
        val candidates: List<Candidate> = emptyList()
    )

    @Serializable
    private data class Candidate(
        val content: ContentOut? = null
    )

    @Serializable
    private data class ContentOut(
        val parts: List<PartOut> = emptyList()
    )

    @Serializable
    private data class PartOut(
        val text: String? = null
    )
}
