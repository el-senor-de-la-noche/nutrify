package com.nutrify.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrify.managers.UsuarioAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class Destino(val ruta: String) {
    object Login : Destino("login")
    object Registro : Destino("registro")
    object Home : Destino("home")
    object Camara : Destino("camara")
    object RegistroDiario : Destino("registro_diario")
    object Perfil : Destino("perfil")
    object IMC : Destino("imc")
    object Configuracion : Destino("configuracion")
    object AcercaDe : Destino("acerca_de")
}

data class NavegacionState(
    val destinoActual: Destino = Destino.Login,
    val destinoAnterior: Destino? = null,
    val estaAutenticado: Boolean = false,
    val mostrarBottomBar: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NavegacionViewModel(
    private val authManager: UsuarioAuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(NavegacionState())
    val state: StateFlow<NavegacionState> = _state.asStateFlow()

    init {
        verificarAutenticacion()
    }

    private fun verificarAutenticacion() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val estaAutenticado = authManager.estaLogeado()
                val destinoInicial = if (estaAutenticado) Destino.Home else Destino.Login

                _state.update {
                    it.copy(
                        destinoActual = destinoInicial,
                        estaAutenticado = estaAutenticado,
                        mostrarBottomBar = estaAutenticado,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        destinoActual = Destino.Login,
                        isLoading = false,
                        errorMessage = "Error al verificar autenticación"
                    )
                }
            }
        }
    }

    fun navegarA(destino: Destino) {
        _state.update {
            it.copy(
                destinoAnterior = it.destinoActual,
                destinoActual = destino,
                errorMessage = null
            )
        }

        // Actualizar estado de bottom bar
        actualizarMostrarBottomBar(destino)
    }

    fun navegarAtras(): Boolean {
        val destinoAnterior = _state.value.destinoAnterior
        return if (destinoAnterior != null) {
            navegarA(destinoAnterior)
            true
        } else {
            false
        }
    }

    fun irALoginDesdeRegistro() {
        _state.update {
            it.copy(
                destinoAnterior = null,
                destinoActual = Destino.Login,
                estaAutenticado = false,
                mostrarBottomBar = false,
                errorMessage = null
            )
        }
    }
    fun navegarAHome() {
        if (_state.value.estaAutenticado) {
            navegarA(Destino.Home)
        } else {
            navegarA(Destino.Login)
        }
    }

    fun onLoginExitoso() {
        _state.update {
            it.copy(
                estaAutenticado = true,
                mostrarBottomBar = true,
                destinoActual = Destino.Home
            )
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            try {
                authManager.logout()
                _state.update {
                    it.copy(
                        estaAutenticado = false,
                        mostrarBottomBar = false,
                        destinoActual = Destino.Login
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Error al cerrar sesión: ${e.message}")
                }
            }
        }
    }

    private fun actualizarMostrarBottomBar(destino: Destino) {
        val mostrar = when (destino) {
            Destino.Home,
            Destino.RegistroDiario,
            Destino.Camara,
            Destino.Perfil,
            Destino.IMC -> _state.value.estaAutenticado

            Destino.Login,
            Destino.Registro,
            Destino.Configuracion,
            Destino.AcercaDe -> false
        }

        _state.update { it.copy(mostrarBottomBar = mostrar) }
    }

    fun obtenerDestinosBottomBar(): List<Destino> {
        return if (_state.value.estaAutenticado) {
            listOf(
                Destino.Home,
                Destino.RegistroDiario,
                Destino.Camara,
                Destino.Perfil,
                Destino.IMC
            )
        } else {
            emptyList()
        }
    }

    fun obtenerTituloPantalla(destino: Destino): String {
        return when (destino) {
            Destino.Login -> "Iniciar Sesión"
            Destino.Registro -> "Registrarse"
            Destino.Home -> "Inicio"
            Destino.Camara -> "Analizar Comida"
            Destino.RegistroDiario -> "Registro Diario"
            Destino.Perfil -> "Mi Perfil"
            Destino.IMC -> "Calculadora IMC"
            Destino.Configuracion -> "Configuración"
            Destino.AcercaDe -> "Acerca de"
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
