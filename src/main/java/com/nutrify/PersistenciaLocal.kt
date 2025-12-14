import com.nutrify.dominio.modelos.RegistroAlimento
import com.nutrify.dominio.modelos.Usuario
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistenciaLocal {
    private val json = Json { prettyPrint = true }
    private val registrosFile = "registros_alimentos.json"
    private val usuariosFile = "usuarios.json"

    // Guardar lista de registros
    fun guardarRegistros(registros: List<RegistroAlimento>): Boolean {
        return try {
            val jsonString = json.encodeToString(registros)
            // En Android: usar FileWriter o SharedPreferences
            // Ejemplo simplificado
            println("Guardando registros: $jsonString")
            true
        } catch (e: Exception) {
            println("Error al guardar registros: ${e.message}")
            false
        }
    }

    // Cargar registros
    fun cargarRegistros(): List<RegistroAlimento> {
        return try {
            // En Android: leer de archivo
            // Ejemplo con datos de prueba
            val jsonString = """[]"""
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            println("Error al cargar registros: ${e.message}")
            emptyList()
        }
    }

    // Guardar usuario
    fun guardarUsuario(usuario: Usuario): Boolean {
        return try {
            val jsonString = json.encodeToString(usuario)
            println("Guardando usuario: $jsonString")
            true
        } catch (e: Exception) {
            false
        }
    }

    // Cargar usuario
    fun cargarUsuario(): Usuario? {
        return try {
            // Implementación real leería de archivo
            null
        } catch (e: Exception) {
            null
        }
    }

    // Método para exportar datos
    fun exportarDatos(formato: String = "JSON"): String {
        val registros = cargarRegistros()
        return when (formato.uppercase()) {
            "JSON" -> json.encodeToString(registros)
            "CSV" -> convertirACSV(registros)
            else -> "Formato no soportado"
        }
    }

    private fun convertirACSV(registros: List<RegistroAlimento>): String {
        val header = "ID,UsuarioID,Fecha,Tipo,Calorías,Proteínas,Fibra"
        val lines = registros.map { registro ->
            "${registro.id},${registro.usuarioId},${registro.fecha}," +
                    "${registro.tipoComida},${registro.caloriasTotales}," +
                    "${registro.proteinasTotales},${registro.fibraTotal}"
        }
        return (listOf(header) + lines).joinToString("\n")
    }
}