package com.nutrify.dominio
import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.dominio.modelos.MacronutrientesObjetivo
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.RegistroAlimento
import kotlin.math.roundToInt
import java.time.LocalDate



class NutricionCalculatorService {

    // Cálculo de calorías diarias (Fórmula de Harris-Benedict)
    fun calcularCaloriasDiarias(usuario: Usuario): Double {
        val tmb = calcularTMB(usuario)
        val factorActividad = obtenerFactorActividad(usuario.nivelActividad)
        var calorias = tmb * factorActividad

        // Ajustar según objetivo
        calorias = when (usuario.objetivo) {
            ObjetivoNutricional.BAJAR_PESO -> calorias * 0.85
            ObjetivoNutricional.MANTENER_PESO -> calorias
            ObjetivoNutricional.SUBIR_PESO -> calorias * 1.15
        }

        return calorias.roundToInt().toDouble()
    }

    private fun calcularTMB(usuario: Usuario): Double {
        return when (usuario.sexo) {
            Sexo.MASCULINO -> {
                88.362 + (13.397 * usuario.peso) + (4.799 * usuario.altura * 100) -
                        (5.677 * calcularEdad(usuario.fechaNacimiento))
            }

            Sexo.FEMENINO -> {
                447.593 + (9.247 * usuario.peso) + (3.098 * usuario.altura * 100) -
                        (4.330 * calcularEdad(usuario.fechaNacimiento))
            }

            Sexo.OTRO -> {
                // Promedio de fórmulas masculina y femenina
                ((88.362 + (13.397 * usuario.peso) + (4.799 * usuario.altura * 100) -
                        (5.677 * calcularEdad(usuario.fechaNacimiento))) +
                        (447.593 + (9.247 * usuario.peso) + (3.098 * usuario.altura * 100) -
                                (4.330 * calcularEdad(usuario.fechaNacimiento)))) / 2
            }
        }
    }

    private fun obtenerFactorActividad(nivel: NivelActividad): Double {
        return when (nivel) {
            NivelActividad.SEDENTARIO -> 1.2
            NivelActividad.LIGERO -> 1.375
            NivelActividad.MODERADO -> 1.55
            NivelActividad.INTENSO -> 1.725
            NivelActividad.MUY_INTENSO -> 1.9
        }
    }

    private fun calcularEdad(fechaNacimiento: LocalDate?): Int {
        require(fechaNacimiento != null) { "La fecha de nacimiento es requerida para calcular la edad" }

        val hoy = LocalDate.now()
        var edad = hoy.year - fechaNacimiento.year
        if (hoy.monthValue < fechaNacimiento.monthValue ||
            (hoy.monthValue == fechaNacimiento.monthValue && hoy.dayOfMonth < fechaNacimiento.dayOfMonth)) {
            edad--
        }
        return edad
    }

        // Cálculo de IMC
        fun calcularIMC(usuario: Usuario): Double {
            require(usuario.altura > 0) { "La altura debe ser mayor a 0" }
            val imc = usuario.peso / (usuario.altura * usuario.altura)
            return (imc * 100).roundToInt() / 100.0
        }

        fun obtenerCategoriaIMC(imc: Double): String {
            return when {
                imc < 18.5 -> "Bajo peso"
                imc < 25 -> "Normal"
                imc < 30 -> "Sobrepeso"
                imc < 35 -> "Obesidad grado I"
                imc < 40 -> "Obesidad grado II"
                else -> "Obesidad grado III"
            }
        }

        // Cálculo de macronutrientes recomendados
        fun calcularMacronutrientesRecomendados(usuario: Usuario): MacronutrientesObjetivo {
            val calorias = calcularCaloriasDiarias(usuario)

            // Distribución según objetivo
            val (porcentajeProteinas, porcentajeCarbs, porcentajeGrasas) =
                when (usuario.objetivo) {
                    ObjetivoNutricional.BAJAR_PESO -> Triple(0.30, 0.40, 0.30)
                    ObjetivoNutricional.MANTENER_PESO -> Triple(0.25, 0.45, 0.30)
                    ObjetivoNutricional.SUBIR_PESO -> Triple(0.25, 0.50, 0.25)
                }

            // Gramos por caloría: Proteínas y carbohidratos 4 kcal/g, Grasas 9 kcal/g
            val proteinas = (calorias * porcentajeProteinas) / 4
            val carbohidratos = (calorias * porcentajeCarbs) / 4
            val grasas = (calorias * porcentajeGrasas) / 9

            return MacronutrientesObjetivo(calorias, proteinas, carbohidratos, grasas)
        }

        // Análisis de progreso
        fun analizarProgreso(
            registros: List<RegistroAlimento>,
            objetivo: MacronutrientesObjetivo
        ): Map<String, Any> {
            val totalCalorias = registros.sumOf { it.caloriasTotales }
            val totalProteinas = registros.sumOf { it.proteinasTotales }

            val porcentajeCalorias = (totalCalorias / objetivo.calorias) * 100
            val porcentajeProteinas = (totalProteinas / objetivo.proteinas) * 100

            val recomendacion = when {
                porcentajeCalorias > 110 -> "Estás consumiendo demasiadas calorías"
                porcentajeCalorias < 90 -> "Necesitas consumir más calorías"
                else -> "Tu consumo calórico es adecuado"
            }

            return mapOf(
                "total_calorias" to totalCalorias,
                "total_proteinas" to totalProteinas,
                "porcentaje_calorias" to porcentajeCalorias,
                "porcentaje_proteinas" to porcentajeProteinas,
                "recomendacion" to recomendacion,
                "dias_analizados" to registros.map { it.fecha }.distinct().size
            )
        }
    }
