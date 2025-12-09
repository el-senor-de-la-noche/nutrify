package com.example.nutrify.Dominio

import java.util.Date

class Usuario(
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val fechaNacimiento: Date,
    val sexo: Sexo,
    var pesoActualKg: Double,
    var alturaCm: Double,
    var nivelActividad: NivelActividad,
    var objetivo: ObjetivoNutricional
) : EntidadBase(id = email) {

    fun calcularIMC(): Double =
        pesoActualKg / (alturaCm * alturaCm)

    fun obtenerCategoriaIMC(): String {
        val imc = calcularIMC()

        return when {
            imc < 18.5 -> "Bajo peso"
            imc < 25   -> "Normal"
            imc < 30   -> "Sobrepeso"
            else       -> "Obesidad"
        }
    }

    fun calcularCaloriasObjetivoDiarias(): Double {
        val edad = 25 // se puede mejorar cuando tengas fecha de nacimiento lista

        val tmb = 10 * pesoActualKg +
                6.25 * alturaCm * 100 -
                5 * edad +
                if (sexo == Sexo.MASCULINO) 5 else -161

        val factor = when (nivelActividad) {
            NivelActividad.SEDENTARIO -> 1.2
            NivelActividad.LIGERO -> 1.375
            NivelActividad.MODERADO -> 1.55
            NivelActividad.INTENSO -> 1.725
            NivelActividad.MUY_INTENSO -> 1.9
        }

        val mantenimiento = tmb * factor

        return when (objetivo) {
            ObjetivoNutricional.BAJAR_PESO -> mantenimiento - 300
            ObjetivoNutricional.MANTENER_PESO -> mantenimiento
            ObjetivoNutricional.SUBIR_PESO -> mantenimiento + 300
        }
    }
}
