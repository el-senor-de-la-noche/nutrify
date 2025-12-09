package com.example.nutrify.ui.ViewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutrify.manager.UsuarioAuthManager
import com.example.nutrify.persistencia.UsuarioRepository

class LoginViewModel(
    private val authManager: UsuarioAuthManager,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var error = mutableStateOf<String?>(null)
    var loginExitoso = mutableStateOf(false)

    fun login() {
        val success = authManager.login(email.value, password.value)
        if (success) {
            usuarioRepo.obtener()?.let {
                loginExitoso.value = true
            }
        } else {
            error.value = "Correo o contraseña incorrectos"
        }
    }
}
