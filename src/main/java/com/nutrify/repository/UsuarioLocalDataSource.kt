package com.nutrify.repository



import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.services.StorageService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UsuarioLocalDataSource(
    private val storageService: StorageService,
    private val usuarioFile: String = "usuarios.json",
    private val sesionFile: String = "sesion_activa.json"
) : UsuarioDataSource {

    private val json = storageService.json

    override fun guardarUsuario(usuario: Usuario): Boolean {
        return try {
            val usuarios = obtenerTodosUsuarios().toMutableList()

            val index = usuarios.indexOfFirst { it.email == usuario.email }
            if (index != -1) usuarios[index] = usuario else usuarios.add(usuario)

            val jsonString = json.encodeToString(usuarios)

            // ✅ devuelve el resultado real del guardado
            storageService.guardarJSON(usuarioFile, jsonString)
        } catch (e: Exception) {
            false
        }
    }

    override  fun actualizarUsuario(usuario: Usuario): Boolean {
        return guardarUsuario(usuario)
    }

    override fun obtenerUsuario(): Usuario? {
        return try {
            val raw = storageService.leerJSON(sesionFile)
            if (raw.isBlank() || raw == "{}" || raw == "\"\"") {
                return null  // No hay sesión activa
            }

            val emailSesion = raw.replace("\"", "").trim()
            if (emailSesion.isEmpty()) {
                return null
            }

            // Buscar usuario por email (esto incluye al invitado)
            return obtenerUsuarioPorEmail(emailSesion)
        } catch (e: Exception) {
            null
        }
    }



    override  fun obtenerUsuarioPorEmail(email: String): Usuario? {
        return try {
            val usuarios = obtenerTodosUsuarios()
            usuarios.firstOrNull { it.email == email }
        } catch (e: Exception) {
            null
        }
    }

    override  fun guardarSesionActiva(email: String) {
        storageService.guardarJSON(sesionFile, "\"$email\"")
    }

    override fun limpiarSesionActiva() {
        storageService.guardarJSON(sesionFile, "")
    }


    override  fun borrarUsuario() {
        // Solo limpiamos la sesión activa, no eliminamos los datos del usuario
        limpiarSesionActiva()
    }

    override  fun existeUsuario(email: String): Boolean {
        return obtenerUsuarioPorEmail(email) != null
    }

    private  fun obtenerTodosUsuarios(): List<Usuario> {
        return try {
            val jsonString = storageService.leerJSON(usuarioFile)
            if (jsonString.isBlank() || jsonString == "{}") {
                emptyList()
            } else {
                json.decodeFromString<List<Usuario>>(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
