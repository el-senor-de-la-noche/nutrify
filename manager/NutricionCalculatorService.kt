package com.example.nutrify.manager

import com.example.nutrify.Dominio.MacronutrientesObjetivo
import com.example.nutrify.Dominio.Usuario

class NutricionCalculatorService {

    fun calcularIMC(usuario: Usuario): Double =
        usuario.pesoActualKg / (usuario.alturaCm * usuario.alturaCm)

    fun calcularMacronutrientes(usuario: Usuario): MacronutrientesObjetivo {
        val calorias = usuario.calcularCaloriasObjetivoDiarias()

        return MacronutrientesObjetivo(
            proteinas = calorias * 0.25 / 4,
            carbohidratos = calorias * 0.45 / 4,
            grasas = calorias * 0.30 / 9
        )
    }
}
