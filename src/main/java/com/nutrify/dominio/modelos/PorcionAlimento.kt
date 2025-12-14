package com.nutrify.dominio.modelos


import com.nutrify.dominio.enums.CategoriaAlimento
import com.nutrify.dominio.enums.UnidadPorcion
import kotlinx.serialization.Serializable

@Serializable
data class PorcionAlimento(
    val alimentoId: String? = null,
    val descripcion: String,
    val cantidad: Double,
    val unidad: UnidadPorcion,
    val categoria: CategoriaAlimento? = null,
    private var _caloriasPorUnidad: Double = 0.0,
    private var _proteinasPorUnidad: Double = 0.0,
    private var _fibraPorUnidad: Double = 0.0
) {
    // Propiedades calculadas
    val calorias: Double get() = cantidad * _caloriasPorUnidad
    val proteinas: Double get() = cantidad * _proteinasPorUnidad
    val fibra: Double get() = cantidad * _fibraPorUnidad

    // Setters para valores nutricionales
    fun setCaloriasPorUnidad(valor: Double) {
        require(valor >= 0) { "Las calorías no pueden ser negativas" }
        _caloriasPorUnidad = valor
    }

    fun setProteinasPorUnidad(valor: Double) {
        require(valor >= 0) { "Las proteínas no pueden ser negativas" }
        _proteinasPorUnidad = valor
    }

    fun setFibraPorUnidad(valor: Double) {
        require(valor >= 0) { "La fibra no puede ser negativa" }
        _fibraPorUnidad = valor
    }
}