package com.nutrify.dominio.modelos

import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Usuario(
    val id: String,
    var nombre: String,
    val email: String,
    val passwordHash: String,

    @Contextual
    var fechaNacimiento: LocalDate? = null,

    var sexo: Sexo = Sexo.OTRO,
    var peso: Double = 70.0,
    var altura: Double = 1.70,
    var nivelActividad: NivelActividad = NivelActividad.SEDENTARIO,
    var objetivo: ObjetivoNutricional = ObjetivoNutricional.MANTENER_PESO,

    // útil para modo invitado
    val esInvitado: Boolean = false
) {

    // ✅ Métodos con validación (pero SIN chocar con setters automáticos)
    fun actualizarNombre(nuevoNombre: String) {
        require(nuevoNombre.isNotBlank()) { "El nombre no puede estar vacío" }
        nombre = nuevoNombre.trim()
    }

    fun actualizarFechaNacimiento(fecha: LocalDate?) {
        if (fecha != null) require(fecha < LocalDate.now()) { "La fecha de nacimiento debe ser en el pasado" }
        fechaNacimiento = fecha
    }

    fun actualizarSexo(nuevoSexo: Sexo) {
        sexo = nuevoSexo
    }

    fun actualizarPeso(nuevoPeso: Double) {
        require(nuevoPeso > 0) { "El peso debe ser positivo" }
        peso = nuevoPeso
    }

    fun actualizarAltura(nuevaAltura: Double) {
        require(nuevaAltura > 0) { "La altura debe ser positiva" }
        altura = nuevaAltura
    }

    fun actualizarNivelActividad(nivel: NivelActividad) {
        nivelActividad = nivel
    }

    fun actualizarObjetivo(nuevoObjetivo: ObjetivoNutricional) {
        objetivo = nuevoObjetivo
    }

    fun verificarPassword(password: String): Boolean {
        // hashing simple para el proyecto
        return password.hashCode().toString() == passwordHash
    }
}
