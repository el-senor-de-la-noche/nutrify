package com.example.nutrify.persistencia   // <-- asegúrate que el folder también sea "persistencia"

import android.content.Context
import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.Dominio.Sexo
import com.example.nutrify.Dominio.ObjetivoNutricional
import com.example.nutrify.Dominio.NivelActividad
import java.util.Date

/**
 * Implementación de IUsuarioDataSource usando SharedPreferences.
 * Guarda/carga al usuario para que persista aunque cierres la app.
 */
class UsuarioPreferencesDataSource(
    context: Context
) : IUsuarioDataSource {

    private val prefs = context.getSharedPreferences("usuario_prefs", Context.MODE_PRIVATE)

    override fun guardarUsuario(usuario: Usuario) {
        prefs.edit()
            .putString("nombre", usuario.nombre)
            .putString("email", usuario.email)
            .putString("passwordHash", usuario.passwordHash)
            // Double -> Float para SharedPreferences
            .putFloat("pesoActualKg", usuario.pesoActualKg.toFloat())
            .putFloat("alturaCm", usuario.alturaCm.toFloat())
            // Date -> Long (epoch millis)
            .putLong("fechaNacimiento", usuario.fechaNacimiento.time)
            // enums como String
            .putString("sexo", usuario.sexo.name)
            .putString("objetivo", usuario.objetivo.name)
            .putString("nivelActividad", usuario.nivelActividad.name)
            .apply()
    }

    override fun obtenerUsuario(): Usuario? {
        // Si no hay nombre o email, asumimos que no hay usuario guardado
        val nombre = prefs.getString("nombre", null) ?: return null
        val email = prefs.getString("email", null) ?: return null

        val passwordHash = prefs.getString("passwordHash", "") ?: ""

        val pesoActualKg = prefs.getFloat("pesoActualKg", 0f).toDouble()
        val alturaCm = prefs.getFloat("alturaCm", 0f).toDouble()

        // Long -> Date
        val fechaMillis = prefs.getLong("fechaNacimiento", -1L)
        val fechaNacimiento = if (fechaMillis > 0L) Date(fechaMillis) else Date(0L)

        val sexoStr = prefs.getString("sexo", Sexo.OTRO.name) ?: Sexo.OTRO.name
        val objetivoStr = prefs.getString("objetivo", ObjetivoNutricional.MANTENER_PESO.name)
            ?: ObjetivoNutricional.MANTENER_PESO.name
        val nivelStr = prefs.getString("nivelActividad", NivelActividad.SEDENTARIO.name)
            ?: NivelActividad.SEDENTARIO.name

        val sexo = runCatching { Sexo.valueOf(sexoStr) }.getOrDefault(Sexo.OTRO)
        val objetivo = runCatching { ObjetivoNutricional.valueOf(objetivoStr) }
            .getOrDefault(ObjetivoNutricional.MANTENER_PESO)
        val nivelActividad = runCatching { NivelActividad.valueOf(nivelStr) }
            .getOrDefault(NivelActividad.SEDENTARIO)

        return Usuario(
            nombre = nombre,
            email = email,
            passwordHash = passwordHash,
            pesoActualKg = pesoActualKg,
            alturaCm = alturaCm,
            fechaNacimiento = fechaNacimiento,   // <-- ahora es Date
            sexo = sexo,
            objetivo = objetivo,
            nivelActividad = nivelActividad
        )
    }

    override fun borrarUsuario() {
        prefs.edit().clear().apply()
    }
}
