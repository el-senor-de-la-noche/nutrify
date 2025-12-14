package com.nutrify.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.enums.*
import com.nutrify.managers.UsuarioAuthManager
import com.nutrify.managers.UsuarioProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate


data class RegistroState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fechaNacimiento: String = "",
    val sexo: Sexo = Sexo.OTRO,
    val peso: String = "",
    val altura: String = "",
    val nivelActividad: NivelActividad = NivelActividad.SEDENTARIO,
    val objetivo: ObjetivoNutricional = ObjetivoNutricional.MANTENER_PESO,
    val isLoading: Boolean = false,
    val isRegistroSuccess: Boolean = false,
    val errorMessage: String? = null,
    val showPassword: Boolean = false,
    val showConfirmPassword: Boolean = false
)

class RegistroViewModel(
    private val authManager: UsuarioAuthManager,
    profileManager: UsuarioProfileManager
) : ViewModel()
{

    private val _state = MutableStateFlow(RegistroState())
    val state: StateFlow<RegistroState> = _state.asStateFlow()

    // Eventos para cambios de campos
    fun onNombreChanged(nombre: String) {
        _state.update { it.copy(nombre = nombre.trim()) }
    }

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email.trim()) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun onFechaNacimientoChanged(fecha: String) {
        _state.update { it.copy(fechaNacimiento = fecha) }
    }

    fun onSexoChanged(sexo: Sexo) {
        _state.update { it.copy(sexo = sexo) }
    }

    fun onPesoChanged(peso: String) {
        _state.update { it.copy(peso = peso) }
    }

    fun onAlturaChanged(altura: String) {
        _state.update { it.copy(altura = altura) }
    }

    fun onNivelActividadChanged(nivel: NivelActividad) {
        _state.update { it.copy(nivelActividad = nivel) }
    }

    fun onObjetivoChanged(objetivo: ObjetivoNutricional) {
        _state.update { it.copy(objetivo = objetivo) }
    }

    fun toggleShowPassword() {
        _state.update { it.copy(showPassword = !it.showPassword) }
    }

    fun toggleShowConfirmPassword() {
        _state.update { it.copy(showConfirmPassword = !it.showConfirmPassword) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun registrar(onSuccess: () -> Unit) {
        val currentState = _state.value
        if (currentState.isLoading) return

        val validationError = validateForm(currentState)
        if (validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val fechaParts = currentState.fechaNacimiento.split("/")
                val fechaNacimiento = LocalDate.of(
                    fechaParts[2].toInt(),
                    fechaParts[1].toInt(),
                    fechaParts[0].toInt()
                )

                authManager.registrar(
                    email = currentState.email,
                    password = currentState.password,
                    nombre = currentState.nombre,
                    fechaNacimiento = fechaNacimiento,
                    sexo = currentState.sexo,
                    peso = currentState.peso.replace(",", ".").toDouble(),
                    altura = currentState.altura.replace(",", ".").toDouble(),
                    nivelActividad = currentState.nivelActividad,
                    objetivo = currentState.objetivo
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        isRegistroSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al registrar"
                    )
                }
            }
        }
    }



    private fun validateForm(state: RegistroState): String? {
        val regexFecha = Regex("""\d{2}/\d{2}/\d{4}""")

        return when {
            state.nombre.isBlank() -> "Ingrese su nombre"
            state.email.isBlank() || !state.email.contains("@") -> "Ingrese un email válido"
            state.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            state.password != state.confirmPassword -> "Las contraseñas no coinciden"
            state.fechaNacimiento.isBlank() -> "Ingrese su fecha de nacimiento"
            !regexFecha.matches(state.fechaNacimiento) -> "Formato fecha: DD/MM/AAAA"
            state.peso.isBlank() || state.peso.replace(",", ".").toDoubleOrNull() == null -> "Ingrese un peso válido"
            state.altura.isBlank() || state.altura.replace(",", ".").toDoubleOrNull() == null -> "Ingrese una altura válida"
            else -> null
        }
    }


    fun resetRegistroSuccess() {
        _state.update { it.copy(isRegistroSuccess = false) }
    }

}

