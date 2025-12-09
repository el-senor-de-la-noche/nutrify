package com.example.nutrify.ui.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutrify.manager.NutricionCalculatorService
import com.example.nutrify.manager.UsuarioAuthManager

class IMCViewModel(
    private val auth: UsuarioAuthManager,
    private val calculator: NutricionCalculatorService
) : ViewModel() {

    var imc = mutableStateOf(0.0)
    var categoria = mutableStateOf("")

    fun calcular() {
        val usuario = auth.obtenerUsuario() ?: return
        imc.value = calculator.calcularIMC(usuario)
        categoria.value = usuario.obtenerCategoriaIMC()
    }
}
