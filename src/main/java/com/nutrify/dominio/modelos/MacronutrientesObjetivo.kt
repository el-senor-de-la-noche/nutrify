package com.nutrify.dominio.modelos


import kotlinx.serialization.Serializable

@Serializable
data class MacronutrientesObjetivo(
    private var _calorias: Double,
    private var _proteinas: Double,
    private var _carbohidratos: Double,
    private var _grasas: Double
) {
    // Getters
    val calorias: Double get() = _calorias
    val proteinas: Double get() = _proteinas
    val carbohidratos: Double get() = _carbohidratos
    val grasas: Double get() = _grasas

    // Setters con validación
    fun setCalorias(calorias: Double) {
        require(calorias >= 0) { "Las calorías no pueden ser negativas" }
        _calorias = calorias
    }

    fun setProteinas(proteinas: Double) {
        require(proteinas >= 0) { "Las proteínas no pueden ser negativas" }
        _proteinas = proteinas
    }

    fun setCarbohidratos(carbohidratos: Double) {
        require(carbohidratos >= 0) { "Los carbohidratos no pueden ser negativos" }
        _carbohidratos = carbohidratos
    }

    fun setGrasas(grasas: Double) {
        require(grasas >= 0) { "Las grasas no pueden ser negativas" }
        _grasas = grasas
    }

    fun calcularPorcentajes(): Map<String, Double> {
        val total = _proteinas + _carbohidratos + _grasas
        return if (total > 0) {
            mapOf(
                "proteinas" to (_proteinas / total * 100),
                "carbohidratos" to (_carbohidratos / total * 100),
                "grasas" to (_grasas / total * 100)
            )
        } else {
            emptyMap()
        }
    }
}