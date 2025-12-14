package com.nutrify.services


import com.nutrify.dominio.enums.*
import com.nutrify.dominio.modelos.*
import java.time.LocalDate
import java.time.Period
import kotlin.math.*

class NutricionCalculatorService {

    // Cálculo de calorías diarias (Fórmula de Mifflin-St Jeor - más moderna)
    fun calcularCaloriasDiarias(usuario: Usuario): Double {
        val tmb = calcularTMBMifflin(usuario)
        val factorActividad = obtenerFactorActividad(usuario.nivelActividad)
        var calorias = tmb * factorActividad

        // Ajustar según objetivo
        calorias = when (usuario.objetivo) {
            ObjetivoNutricional.BAJAR_PESO -> calorias * 0.85  // Déficit de 15%
            ObjetivoNutricional.MANTENER_PESO -> calorias
            ObjetivoNutricional.SUBIR_PESO -> calorias * 1.15  // Superávit de 15%
        }

        return round(calorias)
    }

    // Fórmula de Mifflin-St Jeor
    private fun calcularTMBMifflin(usuario: Usuario): Double {
        val edad = calcularEdad(usuario.fechaNacimiento)
        return when (usuario.sexo) {
            Sexo.MASCULINO -> {
                10 * usuario.peso + 6.25 * usuario.altura * 100 - 5 * edad + 5
            }
            Sexo.FEMENINO -> {
                10 * usuario.peso + 6.25 * usuario.altura * 100 - 5 * edad - 161
            }
            Sexo.OTRO -> {
                // Promedio
                ((10 * usuario.peso + 6.25 * usuario.altura * 100 - 5 * edad + 5) +
                        (10 * usuario.peso + 6.25 * usuario.altura * 100 - 5 * edad - 161)) / 2
            }
        }
    }

    // Fórmula de Katch-McArdle (para personas que conocen su porcentaje de grasa)
    fun calcularCaloriasKatchMcArdle(
        peso: Double,
        porcentajeGrasa: Double,
        nivelActividad: NivelActividad,
        objetivo: ObjetivoNutricional
    ): Double {
        val masaMagra = peso * (1 - porcentajeGrasa / 100)
        val tmb = 370 + (21.6 * masaMagra)
        val factorActividad = obtenerFactorActividad(nivelActividad)
        var calorias = tmb * factorActividad

        return when (objetivo) {
            ObjetivoNutricional.BAJAR_PESO -> round(calorias * 0.85)
            ObjetivoNutricional.MANTENER_PESO -> round(calorias)
            ObjetivoNutricional.SUBIR_PESO -> round(calorias * 1.15)
        }
    }

    // Cálculo de IMC con categoría detallada
    fun calcularIMC(usuario: Usuario): Map<String, Any> {
        require(usuario.altura > 0) { "La altura debe ser mayor a 0" }
        val imc = usuario.peso / (usuario.altura * usuario.altura)
        val imcRedondeado = round(imc * 100) / 100

        val categoria = obtenerCategoriaIMC(imcRedondeado)
        val riesgo = obtenerRiesgoSaludIMC(imcRedondeado)
        val pesoIdeal = calcularPesoIdeal(usuario.altura)

        return mapOf(
            "imc" to imcRedondeado,
            "categoria" to categoria,
            "riesgo" to riesgo,
            "peso_ideal_min" to pesoIdeal.first,
            "peso_ideal_max" to pesoIdeal.second,
            "diferencia_peso" to mapOf(
                "bajo_peso" to max(0.0, pesoIdeal.first - usuario.peso),
                "sobrepeso" to max(0.0, usuario.peso - pesoIdeal.second)
            )
        )
    }

    private fun obtenerRiesgoSaludIMC(imc: Double): String {
        return when {
            imc < 18.5 -> "Riesgo moderado de otras condiciones clínicas"
            imc < 25 -> "Riesgo promedio"
            imc < 30 -> "Riesgo aumentado"
            imc < 35 -> "Riesgo alto"
            imc < 40 -> "Riesgo muy alto"
            else -> "Riesgo extremadamente alto"
        }
    }

    private fun calcularPesoIdeal(altura: Double): Pair<Double, Double> {
        val imcMin = 18.5
        val imcMax = 24.9
        return Pair(
            round(imcMin * altura * altura),
            round(imcMax * altura * altura)
        )
    }

    // Cálculo de porcentaje de grasa corporal (Fórmula de Deurenberg)
    fun calcularPorcentajeGrasa(usuario: Usuario, imc: Double): Double {
        val edad = calcularEdad(usuario.fechaNacimiento)
        val porcentaje = when (usuario.sexo) {
            Sexo.MASCULINO -> (1.20 * imc) + (0.23 * edad) - 16.2
            Sexo.FEMENINO -> (1.20 * imc) + (0.23 * edad) - 5.4
            Sexo.OTRO -> ((1.20 * imc) + (0.23 * edad) - 10.8) // Promedio
        }
        return porcentaje.coerceIn(5.0, 60.0)
    }

    // Cálculo de agua diaria recomendada (Fórmula simple)
    fun calcularAguaDiaria(usuario: Usuario): Double {
        // 35 ml por kg de peso
        return round(usuario.peso * 35.0 / 1000.0) // Convertir a litros
    }

    // Cálculo de metabolismo basal ajustado por composición corporal
    fun calcularMetabolismoBasal(usuario: Usuario, porcentajeGrasa: Double? = null): Map<String, Double> {
        val tmbMifflin = calcularTMBMifflin(usuario)
        val tmbHarris = calcularTMBHarris(usuario)

        val resultados = mutableMapOf(
            "mifflin_st_jeor" to tmbMifflin,
            "harris_benedict" to tmbHarris,
            "promedio" to (tmbMifflin + tmbHarris) / 2
        )

        porcentajeGrasa?.let {
            val tmbKatch = calcularTMBKatchMcArdle(usuario.peso, it)
            resultados["katch_mcardle"] = tmbKatch
            resultados["recomendado"] = tmbKatch // Más preciso si se conoce grasa corporal
        }

        return resultados
    }

    private fun calcularTMBHarris(usuario: Usuario): Double {
        val edad = calcularEdad(usuario.fechaNacimiento)
        return when (usuario.sexo) {
            Sexo.MASCULINO -> {
                88.362 + (13.397 * usuario.peso) + (4.799 * usuario.altura * 100) - (5.677 * edad)
            }
            Sexo.FEMENINO -> {
                447.593 + (9.247 * usuario.peso) + (3.098 * usuario.altura * 100) - (4.330 * edad)
            }
            Sexo.OTRO -> {
                ((88.362 + (13.397 * usuario.peso) + (4.799 * usuario.altura * 100) - (5.677 * edad)) +
                        (447.593 + (9.247 * usuario.peso) + (3.098 * usuario.altura * 100) - (4.330 * edad))) / 2
            }
        }
    }

    private fun calcularTMBKatchMcArdle(peso: Double, porcentajeGrasa: Double): Double {
        val masaMagra = peso * (1 - porcentajeGrasa / 100)
        return 370 + (21.6 * masaMagra)
    }

    // Cálculo de macronutrientes con diferentes distribuciones
    fun calcularMacronutrientesRecomendados(
        usuario: Usuario,
        distribucion: String = "estandar"
    ): MacronutrientesObjetivo {
        val calorias = calcularCaloriasDiarias(usuario)

        val (pProteinas, pCarbs, pGrasas) = when (distribucion) {
            "alta_proteina" -> Triple(0.35, 0.40, 0.25)
            "baja_carbohidratos" -> Triple(0.30, 0.25, 0.45)
            "equilibrada" -> Triple(0.25, 0.45, 0.30)
            "atleta" -> Triple(0.30, 0.50, 0.20)
            else -> Triple(0.25, 0.45, 0.30) // estándar
        }

        // Gramos por caloría: Proteínas 4 kcal/g, Carbohidratos 4 kcal/g, Grasas 9 kcal/g
        val proteinas = (calorias * pProteinas) / 4
        val carbohidratos = (calorias * pCarbs) / 4
        val grasas = (calorias * pGrasas) / 9

        return MacronutrientesObjetivo(calorias, proteinas, carbohidratos, grasas)
    }

    // Análisis de progreso con métricas avanzadas
    fun analizarProgresoAvanzado(
        registros: List<RegistroAlimento>,
        objetivo: MacronutrientesObjetivo,
        diasAnalizados: Int
    ): Map<String, Any> {
        if (registros.isEmpty() || diasAnalizados == 0) {
            return mapOf("error" to "No hay datos suficientes para analizar")
        }

        val totalCalorias = registros.sumOf { it.caloriasTotales }
        val totalProteinas = registros.sumOf { it.proteinasTotales }
        val totalCarbs = registros.sumOf {
            when (it) {
                is RegistroIA -> it.analisis.carbohidratos ?: 0.0
                else -> 0.0
            }
        }
        val totalGrasas = registros.sumOf {
            when (it) {
                is RegistroIA -> it.analisis.grasas ?: 0.0
                else -> 0.0
            }
        }

        val promedioDiarioCalorias = totalCalorias / diasAnalizados
        val promedioDiarioProteinas = totalProteinas / diasAnalizados

        val porcentajeCalorias = (promedioDiarioCalorias / objetivo.calorias) * 100
        val porcentajeProteinas = (promedioDiarioProteinas / objetivo.proteinas) * 100

        val consistencia = calcularConsistencia(registros, diasAnalizados)
        val variedadAlimentos = calcularVariedadAlimentos(registros)

        val recomendaciones = mutableListOf<String>()

        when {
            porcentajeCalorias > 115 -> recomendaciones.add("Reduce tu ingesta calórica en un 15%")
            porcentajeCalorias < 85 -> recomendaciones.add("Aumenta tu ingesta calórica en un 15%")
            porcentajeCalorias in 85.0..115.0 -> recomendaciones.add("Tu ingesta calórica es adecuada")
        }

        when {
            porcentajeProteinas > 120 -> recomendaciones.add("Tu consumo de proteínas es muy alto")
            porcentajeProteinas < 80 -> recomendaciones.add("Aumenta tu consumo de proteínas")
            else -> recomendaciones.add("Tu consumo de proteínas es adecuado")
        }

        if (consistencia < 70) {
            recomendaciones.add("Mejora la consistencia en tus registros diarios")
        }

        if (variedadAlimentos < 10) {
            recomendaciones.add("Aumenta la variedad de alimentos en tu dieta")
        }

        return mapOf(
            "metricas" to mapOf(
                "calorias_diarias_promedio" to promedioDiarioCalorias,
                "proteinas_diarias_promedio" to promedioDiarioProteinas,
                "porcentaje_objetivo_calorias" to porcentajeCalorias,
                "porcentaje_objetivo_proteinas" to porcentajeProteinas,
                "consistencia_registro" to consistencia,
                "variedad_alimentos" to variedadAlimentos,
                "dias_registrados" to diasAnalizados
            ),
            "recomendaciones" to recomendaciones,
            "tendencias" to analizarTendencias(registros, diasAnalizados)
        )
    }

    private fun calcularConsistencia(registros: List<RegistroAlimento>, diasTotales: Int): Double {
        val diasConRegistros = registros.map { it.fecha }.distinct().size
        return (diasConRegistros.toDouble() / diasTotales) * 100
    }

    private fun calcularVariedadAlimentos(registros: List<RegistroAlimento>): Int {
        val alimentos = registros.flatMap { registro ->
            when (registro) {
                is RegistroManual -> registro.porciones.map { it.descripcion }
                is RegistroIA -> listOf(registro.analisis.descripcion ?: "")
                else -> emptyList()
            }
        }.distinct()

        return alimentos.size
    }

    private fun analizarTendencias(registros: List<RegistroAlimento>, dias: Int): Map<String, Any> {
        if (dias < 7) return emptyMap()

        val registrosPorDia = registros.groupBy { it.fecha }
        val caloriasPorDia = registrosPorDia.mapValues { (_, registrosDia) ->
            registrosDia.sumOf { it.caloriasTotales }
        }

        val promedioMovil = mutableListOf<Double>()
        val valores = caloriasPorDia.values.toList()

        for (i in 6 until valores.size) {
            val promedio = valores.subList(i-6, i+1).average()
            promedioMovil.add(promedio)
        }

        val tendencia = if (promedioMovil.size >= 2) {
            val ultimo = promedioMovil.last()
            val penultimo = promedioMovil[promedioMovil.size - 2]
            when {
                ultimo > penultimo * 1.05 -> "ascendente"
                ultimo < penultimo * 0.95 -> "descendente"
                else -> "estable"
            }
        } else {
            "insuficiente_datos"
        }

        return mapOf(
            "tendencia_calorias" to tendencia,
            "promedio_movil_7_dias" to promedioMovil,
            "variacion_diaria" to calcularVariacionDiaria(valores)
        )
    }

    private fun calcularVariacionDiaria(valores: List<Double>): Double {
        if (valores.size < 2) return 0.0

        val diferencias = mutableListOf<Double>()
        for (i in 1 until valores.size) {
            val variacion = abs(valores[i] - valores[i-1]) / valores[i-1] * 100
            diferencias.add(variacion)
        }

        return diferencias.average()
    }

    // Cálculo de agua recomendada por actividad
    fun calcularAguaRecomendada(
        peso: Double,
        nivelActividad: NivelActividad,
        climaCaliente: Boolean = false
    ): Double {
        var aguaBase = peso * 35.0 // 35 ml por kg

        when (nivelActividad) {
            NivelActividad.LIGERO -> aguaBase *= 1.1
            NivelActividad.MODERADO -> aguaBase *= 1.2
            NivelActividad.INTENSO -> aguaBase *= 1.3
            NivelActividad.MUY_INTENSO -> aguaBase *= 1.4
            else -> {}
        }

        if (climaCaliente) {
            aguaBase *= 1.2
        }

        return aguaBase / 1000.0 // Convertir a litros
    }

    // Cálculo de déficit/superávit para objetivo
    fun calcularCaloriasObjetivo(
        usuario: Usuario,
        objetivo: ObjetivoNutricional,
        tasaCambio: Double = 0.5 // kg por semana
    ): Double {
        val caloriasMantenimiento = calcularCaloriasDiarias(usuario)

        // 1 kg de grasa ≈ 7700 kcal
        val cambioCaloriasSemana = tasaCambio * 7700
        val cambioCaloriasDia = cambioCaloriasSemana / 7

        return when (objetivo) {
            ObjetivoNutricional.BAJAR_PESO -> round(caloriasMantenimiento - cambioCaloriasDia)
            ObjetivoNutricional.MANTENER_PESO -> caloriasMantenimiento
            ObjetivoNutricional.SUBIR_PESO -> round(caloriasMantenimiento + cambioCaloriasDia)
        }
    }

    private fun calcularEdad(fechaNacimiento: LocalDate?): Int {
        if (fechaNacimiento == null) {
            // Valor por defecto si no hay fecha de nacimiento
            return 30
        }
        val hoy = LocalDate.now()
        return Period.between(fechaNacimiento, hoy).years
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

    private fun obtenerCategoriaIMC(imc: Double): String {
        return when {
            imc < 16.0 -> "Delgadez severa"
            imc < 17.0 -> "Delgadez moderada"
            imc < 18.5 -> "Delgadez leve"
            imc < 25.0 -> "Normal"
            imc < 30.0 -> "Sobrepeso"
            imc < 35.0 -> "Obesidad grado I"
            imc < 40.0 -> "Obesidad grado II"
            else -> "Obesidad grado III"
        }
    }

    private fun round(value: Double): Double {
        return (value * 100).roundToInt() / 100.0
    }
}