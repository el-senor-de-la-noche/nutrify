package com.nutrify.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.dominio.enums.NivelActividad
import com.nutrify.dominio.enums.ObjetivoNutricional
import com.nutrify.dominio.enums.Sexo
import com.nutrify.dominio.modelos.Usuario
import com.nutrify.managers.UsuarioProfileManager
import com.nutrify.services.NutricionCalculatorService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class PerfilState(
    val usuario: Usuario? = null,
    val nombre: String = "",
    val email: String = "",
    val fechaNacimiento: String = "",
    val sexo: Sexo = Sexo.OTRO,
    val peso: String = "",
    val altura: String = "",
    val nivelActividad: NivelActividad = NivelActividad.SEDENTARIO,
    val objetivo: ObjetivoNutricional = ObjetivoNutricional.MANTENER_PESO,
    val isLoading: Boolean = false,
    val isEditando: Boolean = false,
    val isGuardado: Boolean = false,
    val errorMessage: String? = null,
    val mostrarDialogoConfirmacion: Boolean = false
)

class PerfilViewModel(
    private val profileManager: UsuarioProfileManager,
    private val calculator: NutricionCalculatorService
) : ViewModel() {

    private val _state = MutableStateFlow(PerfilState())
    val state: StateFlow<PerfilState> = _state.asStateFlow()

    init {
        cargarPerfilUsuario()
    }

    fun cargarPerfilUsuario() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val usuario = profileManager.obtenerUsuario()

                _state.update {
                    it.copy(
                        usuario = usuario,
                        nombre = usuario.nombre,
                        email = usuario.email,
                        fechaNacimiento = usuario.fechaNacimiento?.toString() ?: "",
                        sexo = usuario.sexo,
                        peso = usuario.peso.toString(),
                        altura = usuario.altura.toString(),
                        nivelActividad = usuario.nivelActividad,
                        objetivo = usuario.objetivo,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar perfil: ${e.message}"
                    )
                }
            }
        }
    }

    fun onNombreChanged(nombre: String) {
        _state.update { it.copy(nombre = nombre.trim()) }
    }

    fun onFechaNacimientoChanged(fecha: String) {
        _state.update { it.copy(fechaNacimiento = fecha.trim()) }
    }

    fun onSexoChanged(sexo: Sexo) {
        _state.update { it.copy(sexo = sexo) }
    }

    fun onPesoChanged(peso: String) {
        _state.update { it.copy(peso = peso.trim()) }
    }

    fun onAlturaChanged(altura: String) {
        _state.update { it.copy(altura = altura.trim()) }
    }

    fun onNivelActividadChanged(nivel: NivelActividad) {
        _state.update { it.copy(nivelActividad = nivel) }
    }

    fun onObjetivoChanged(objetivo: ObjetivoNutricional) {
        _state.update { it.copy(objetivo = objetivo) }
    }

    fun toggleEditando() {
        _state.update { it.copy(isEditando = !it.isEditando) }
    }

    fun mostrarDialogoConfirmacion() {
        _state.update { it.copy(mostrarDialogoConfirmacion = true) }
    }

    fun ocultarDialogoConfirmacion() {
        _state.update { it.copy(mostrarDialogoConfirmacion = false) }
    }

    fun guardarCambios() {
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
                val peso = currentState.peso.toDoubleOrNull() ?: 0.0
                val altura = currentState.altura.toDoubleOrNull() ?: 0.0

                val fechaNacimiento: LocalDate? = parseFechaNacimientoOrNull(currentState.fechaNacimiento)

                val success = profileManager.actualizarPerfilCompleto(
                    nombre = currentState.nombre,
                    fechaNacimiento = fechaNacimiento,
                    sexo = currentState.sexo,
                    peso = peso,
                    altura = altura,
                    nivelActividad = currentState.nivelActividad,
                    objetivo = currentState.objetivo
                )

                if (success) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isEditando = false,
                            isGuardado = true,
                            mostrarDialogoConfirmacion = false
                        )
                    }

                    // ✅ Recargar desde persistencia para que Home/IMC vean lo nuevo
                    cargarPerfilUsuario()

                    viewModelScope.launch {
                        delay(2000)
                        _state.update { st -> st.copy(isGuardado = false) }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo guardar el perfil"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar cambios: ${e.message}"
                    )
                }
            }
        }
    }

    private fun validateForm(state: PerfilState): String? {
        return when {
            state.nombre.isBlank() -> "Ingrese su nombre"
            state.peso.isBlank() || state.peso.toDoubleOrNull() == null || state.peso.toDouble() <= 0 -> "Ingrese un peso válido"
            state.altura.isBlank() || state.altura.toDoubleOrNull() == null || state.altura.toDouble() <= 0 -> "Ingrese una altura válida"
            // ✅ fechaNacimiento es opcional (si viene vacía, se guarda como null)
            else -> null
        }
    }

    private fun parseFechaNacimientoOrNull(raw: String): LocalDate? {
        val v = raw.trim()
        if (v.isEmpty()) return null

        // 1) ISO: AAAA-MM-DD
        try {
            return LocalDate.parse(v)
        } catch (_: DateTimeParseException) {
        }

        // 2) DD/MM/AAAA o DD-MM-AAAA o DD.MM.AAAA
        val parts = v.split("/", "-", ".").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.size == 3) {
            val day = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            val year = parts[2].toIntOrNull()
            if (day != null && month != null && year != null) {
                return LocalDate.of(year, month, day)
            }
        }

        throw IllegalArgumentException("Formato de fecha inválido")
    }

    fun calcularRecomendacionesNutricionales(): Map<String, Any> {
        val usuario = _state.value.usuario ?: return emptyMap()
        return try {
            val caloriasDiarias = calculator.calcularCaloriasDiarias(usuario)
            val macronutrientes = calculator.calcularMacronutrientesRecomendados(usuario)
            val imcResult = calculator.calcularIMC(usuario)
            val aguaDiaria = calculator.calcularAguaDiaria(usuario)

            mapOf(
                "calorias_diarias" to caloriasDiarias,
                "macronutrientes" to macronutrientes,
                "imc" to imcResult,
                "agua_diaria_litros" to aguaDiaria,
                "recomendaciones" to generarRecomendaciones(usuario)
            )
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun generarRecomendaciones(usuario: Usuario): List<String> {
        val recomendaciones = mutableListOf<String>()
        return try {
            val imcResult = calculator.calcularIMC(usuario)
            val imcValue = imcResult["imc"] as? Double ?: 0.0

            val categoriaIMC = when {
                imcValue < 18.5 -> "Bajo peso"
                imcValue < 25 -> "Normal"
                imcValue < 30 -> "Sobrepeso"
                else -> "Obesidad"
            }

            when (categoriaIMC) {
                "Bajo peso" -> recomendaciones.add("Considere aumentar su ingesta calórica")
                "Normal" -> recomendaciones.add("Mantenga sus hábitos actuales")
                "Sobrepeso" -> recomendaciones.add("Considere reducir su ingesta calórica")
                "Obesidad" -> recomendaciones.add("Consulte con un profesional de la salud")
            }

            when (usuario.objetivo) {
                ObjetivoNutricional.BAJAR_PESO -> recomendaciones.add("Mantenga un déficit calórico moderado")
                ObjetivoNutricional.MANTENER_PESO -> recomendaciones.add("Mantenga el equilibrio calórico")
                ObjetivoNutricional.SUBIR_PESO -> recomendaciones.add("Aumente su ingesta calórica progresivamente")
            }

            recomendaciones
        } catch (_: Exception) {
            listOf("No se pudieron generar recomendaciones")
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
