package com.example.nutrify.ui.ViewModels
import androidx.lifecycle.ViewModel
import com.example.nutrify.Dominio.RegistroComida
import com.example.nutrify.manager.RegistroComidaManager
import com.example.nutrify.manager.UsuarioAuthManager
import java.util.Date

class HomeViewModel(
    private val registroManager: RegistroComidaManager,
    private val auth: UsuarioAuthManager
) : ViewModel() {

    fun obtenerConsumoHoy(): List<RegistroComida> {
        val usuario = auth.obtenerUsuario() ?: return emptyList()
        return registroManager.registrosPorDia(usuario.email, Date())
    }
}
