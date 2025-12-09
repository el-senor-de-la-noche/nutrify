package com.example.nutrify.ui.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutrify.Dominio.ObjetivoNutricional
import com.example.nutrify.Dominio.Usuario
import com.example.nutrify.manager.UsuarioProfileManager

class PerfilViewModel(
    private val profileManager: UsuarioProfileManager
) : ViewModel() {

    var usuario = mutableStateOf<Usuario?>(null)

    init {
        usuario.value = profileManager.obtenerUsuario()
    }

    fun actualizarPeso(peso: Double) {
        profileManager.actualizarPeso(peso)
    }

    fun actualizarAltura(altura: Double) {
        profileManager.actualizarAltura(altura)
    }

    fun actualizarObjetivo(obj: ObjetivoNutricional) {
        profileManager.actualizarObjetivo(obj)
    }
}
