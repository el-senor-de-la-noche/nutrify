package com.nutrify.managers

import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.repository.UsuarioRepository
import com.nutrify.services.StorageService
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.UUID

class UsuarioAuthManager(
    private val usuarioRepo: UsuarioRepository,
    private val storage: StorageService
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val sessionFile = "session_usuario.json"
    private val guestEmail = "guest@nutrify.local"

    private var usuarioActual: Usuario? = null

    @Serializable
    private data class Session(val email: String? = null)

    init {
        cargarSesion()
        if (usuarioActual == null) {
            entrarComoInvitado()
        }
    }

    // =========================
    // Lecturas de estado
    // =========================
    fun obtenerUsuarioSesionActiva(): Usuario? = usuarioActual
    fun obtenerUsuarioActual(): Usuario? = usuarioActual // alias para ViewModels

    fun estaLogeado(): Boolean = usuarioActual != null
    fun esInvitado(): Boolean = (usuarioActual?.email == guestEmail)

    // =========================
    // Auth
    // =========================
    fun logout() {
        usuarioActual = null

        // Limpia sesión persistida
        storage.guardarJSON(sessionFile, json.encodeToString(Session(email = null)))
        usuarioRepo.cerrarSesion()

        // Vuelve a invitado automáticamente
        entrarComoInvitado()
    }

    fun entrarComoInvitado(): Usuario {
        val existente = usuarioRepo.obtenerUsuarioPorEmail(guestEmail)

        val invitado = existente ?: Usuario(
            id = UUID.randomUUID().toString(),
            nombre = "Invitado",
            email = guestEmail,
            passwordHash = "",
            fechaNacimiento = LocalDate.parse("2000-01-01"),
            sexo = Sexo.OTRO,
            peso = 70.0,
            altura = 1.70,
            nivelActividad = NivelActividad.SEDENTARIO,
            objetivo = ObjetivoNutricional.MANTENER_PESO,
            esInvitado = true
        ).also { usuarioRepo.guardarUsuario(it) }

        usuarioActual = invitado

        // Marca sesión activa (repo) + archivo session_usuario.json
        usuarioRepo.guardarSesionActiva(guestEmail)
        storage.guardarJSON(sessionFile, json.encodeToString(Session(guestEmail)))

        return invitado
    }

    fun login(email: String, password: String): Boolean {
        val usuario = usuarioRepo.iniciarSesion(email.trim(), password) ?: return false
        usuarioActual = usuario
        storage.guardarJSON(sessionFile, json.encodeToString(Session(usuario.email)))
        return true
    }

    /**
     * ✅ ESTE ES EL MÉTODO QUE TU RegistroViewModel ESTÁ LLAMANDO
     */
    fun registrar(
        email: String,
        password: String,
        nombre: String,
        fechaNacimiento: LocalDate?,
        sexo: Sexo,
        peso: Double,
        altura: Double,
        nivelActividad: NivelActividad,
        objetivo: ObjetivoNutricional
    ): Boolean {
        val correo = email.trim().lowercase()

        if (correo.isBlank() || !correo.contains("@")) return false
        if (password.length < 6) return false
        if (usuarioRepo.existeUsuario(correo)) return false

        val usuario = Usuario(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            email = correo,
            passwordHash = password.hashCode().toString(), // simple, suficiente para tu proyecto
            fechaNacimiento = fechaNacimiento,
            sexo = sexo,
            peso = peso,
            altura = altura,
            nivelActividad = nivelActividad,
            objetivo = objetivo,
            esInvitado = false
        )

        return registrar(usuario)
    }

    /**
     * Permite registrar pasando el objeto (lo usas en otras partes).
     */
    fun registrar(usuario: Usuario): Boolean {
        val ok = usuarioRepo.guardarUsuario(usuario)
        if (!ok) return false

        usuarioActual = usuario
        usuarioRepo.guardarSesionActiva(usuario.email)
        storage.guardarJSON(sessionFile, json.encodeToString(Session(usuario.email)))

        return true
    }

    // =========================
    // Sesión persistida
    // =========================
    private fun cargarSesion() {
        val raw = storage.leerJSON(sessionFile)
        if (raw.isBlank() || raw == "{}") return

        runCatching {
            val s = json.decodeFromString<Session>(raw)
            val email = s.email ?: return
            usuarioActual = usuarioRepo.obtenerUsuarioPorEmail(email)
        }
    }
}
