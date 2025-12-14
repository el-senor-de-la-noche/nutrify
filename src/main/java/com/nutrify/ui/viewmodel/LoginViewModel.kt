package com.nutrify.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.managers.UsuarioAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showPassword: Boolean = false
)
class LoginViewModel(
    private val authManager: UsuarioAuthManager
)
 : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()




    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email.trim()) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun toggleShowPassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun login() {
        val currentState = _state.value
        if (currentState.isLoading) return

        // Validaciones básicas
        if (currentState.email.isBlank() || !currentState.email.contains("@")) {
            _state.update { it.copy(errorMessage = "Ingrese un email válido") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val usuario = authManager.login(
                    email = currentState.email,
                    password = currentState.password
                )

                if (usuario != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = true,
                            errorMessage = null
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccess = false,
                            errorMessage = "Credenciales incorrectas"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al iniciar sesión: ${e.message}"
                    )
                }
            }
        }
    }
    fun entrarComoInvitado() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                authManager.entrarComoInvitado()
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo entrar como invitado: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetLoginSuccess() {
        _state.update { it.copy(isLoginSuccess = false) }
    }

    fun isLoggedIn(): Boolean {
        return authManager.estaLogeado()
    }

     fun getCurrentUserEmail(): String? {
        return authManager.obtenerUsuarioActual()?.email
    }
}
