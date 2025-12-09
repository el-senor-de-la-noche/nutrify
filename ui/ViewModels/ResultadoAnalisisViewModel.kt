package com.example.nutrify.ui.ViewModels

import androidx.lifecycle.ViewModel
import com.example.nutrify.Dominio.AnalisisNutricional
import com.example.nutrify.Dominio.PorcionAlimento
import com.example.nutrify.Dominio.RegistroComida
import com.example.nutrify.Dominio.UnidadPorcion
import com.example.nutrify.manager.UsuarioAuthManager
import java.util.Date
import com.example.nutrify.manager.*

class ResultadoAnalisisViewModel(
    private val registroManager: RegistroComidaManager,
    private val auth: UsuarioAuthManager
) : ViewModel() {

    fun agregarARegistro(analisis: AnalisisNutricional) {
        val usuario = auth.obtenerUsuario() ?: return

        val porcion = PorcionAlimento(
            alimentoNombre = analisis.alimentoDetectado,
            cantidad = 1.0,
            unidad = UnidadPorcion.PORCION,
            caloriasTotales = analisis.caloriasEstimadas,
            proteinasTotales = analisis.proteinasEstimadas,
            carbohidratosTotales = analisis.carbohidratosEstimados,
            grasasTotales = analisis.grasasEstimadas,
            fibraTotal = analisis.fibraEstimada
        )


        val registro = RegistroComida(
            usuarioId = usuario.email,
            fechaHora = Date(),
            porciones = listOf(porcion)
        )

        registroManager.agregarRegistro(registro)
    }
}
