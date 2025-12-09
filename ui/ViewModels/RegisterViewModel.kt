package com.example.nutrify.ui.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutrify.manager.UsuarioAuthManager
import com.example.nutrify.persistencia.UsuarioRepository

class RegisterViewModel(
    private val authManager: UsuarioAuthManager,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {

    var registroExitoso = mutableStateOf(false)
    var error = mutableStateOf<String?>(null)

    fun registrar(nombre: String, email: String, password: String) {
        val usuario = authManager.registrar(nombre, email, password)
        usuarioRepo.guardar(usuario)
        registroExitoso.value = true
    }
}
