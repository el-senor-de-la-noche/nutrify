package com.example.nutrify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nutrify.manager.AnalizadorIAService
import com.example.nutrify.manager.NutricionCalculatorService
import com.example.nutrify.manager.RegistroComidaManager
import com.example.nutrify.manager.UsuarioAuthManager
import com.example.nutrify.manager.UsuarioProfileManager
import com.example.nutrify.persistencia.RegistroComidaMemoryDataSource
import com.example.nutrify.persistencia.RegistroComidaRepository
import com.example.nutrify.persistencia.UsuarioPreferencesDataSource
import com.example.nutrify.persistencia.UsuarioRepository
import com.example.nutrify.ui.ViewModels.CamaraViewModel
import com.example.nutrify.ui.ViewModels.HomeViewModel
import com.example.nutrify.ui.ViewModels.IMCViewModel
import com.example.nutrify.ui.ViewModels.LoginViewModel
import com.example.nutrify.ui.ViewModels.PerfilViewModel
import com.example.nutrify.ui.ViewModels.RegisterViewModel
import com.example.nutrify.ui.ViewModels.RegistroDiarioViewModel
import com.example.nutrify.ui.ViewModels.ResultadoAnalisisViewModel
import com.example.nutrify.ui.screens.CamaraScreen
import com.example.nutrify.ui.screens.HomeScreen
import com.example.nutrify.ui.screens.IMCScreen
import com.example.nutrify.ui.screens.LoginScreen
import com.example.nutrify.ui.screens.PerfilScreen
import com.example.nutrify.ui.screens.RegisterScreen
import com.example.nutrify.ui.screens.RegistroDiarioScreen
import com.example.nutrify.ui.screens.ResultadoAnalisisScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CAMARA = "camara"
    const val RESULTADO = "resultado"
    const val REGISTRO = "registro"
    const val PERFIL = "perfil"
    const val IMC = "imc"
}

@Composable
fun Navigation(navController: NavHostController) {

    val context = LocalContext.current

    // ========= DataSources & Repos =========
    val usuarioRepository = remember {
        UsuarioRepository(
            UsuarioPreferencesDataSource(context)
        )
    }

    // Si más adelante haces un RegistroComidaPreferencesDataSource,
    // cámbialo aquí. Por ahora uso el de memoria:
    val registroComidaRepository = remember {
        RegistroComidaRepository(
            RegistroComidaMemoryDataSource()
        )
    }

    // ========= Managers =========
    val authManager = remember { UsuarioAuthManager(usuarioRepository) }
    val profileManager = remember { UsuarioProfileManager(usuarioRepository) }
    val registroManager = remember { RegistroComidaManager(registroComidaRepository) }
    val calculadora = remember { NutricionCalculatorService() }
    val analizadorIA = remember { AnalizadorIAService() } // sin baseUrl / backend

    // ========= ViewModels =========
    val loginVM = viewModel { LoginViewModel(authManager, usuarioRepository) }
    val registerVM = viewModel { RegisterViewModel(authManager, usuarioRepository) }
    val homeVM = viewModel { HomeViewModel(registroManager, authManager) }
    val camaraVM = viewModel { CamaraViewModel(analizadorIA) }
    val resultadoVM = viewModel { ResultadoAnalisisViewModel(registroManager, authManager) }
    val registroDiarioVM = viewModel { RegistroDiarioViewModel(registroManager, authManager) }
    val perfilVM = viewModel { PerfilViewModel(profileManager) }
    val imcVM = viewModel { IMCViewModel(authManager, calculadora) }

    // ========= Start destination dinámica (sesión persistente) =========
    var startDestination by remember { mutableStateOf(Rutas.LOGIN) }

    LaunchedEffect(Unit) {
        // si hay usuario guardado, parte en HOME
        startDestination = if (usuarioRepository.obtener() != null) {
            Rutas.HOME
        } else {
            Rutas.LOGIN
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ---------- LOGIN ----------
        composable(Rutas.LOGIN) {
            LoginScreen(
                email = loginVM.email.value,
                password = loginVM.password.value,
                error = loginVM.error.value,
                onEmailChange = { loginVM.email.value = it },
                onPasswordChange = { loginVM.password.value = it },
                onLoginClick = {
                    loginVM.login()
                    if (loginVM.loginExitoso.value) {
                        navController.navigate(Rutas.HOME) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Rutas.REGISTER)
                }
            )
        }

        // ---------- REGISTER ----------
        composable(Rutas.REGISTER) {
            RegisterScreen(
                error = registerVM.error.value,
                onRegisterSubmit = { nombre, email, pass ->
                    registerVM.registrar(nombre, email, pass)
                    if (registerVM.registroExitoso.value) {
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(Rutas.REGISTER) { inclusive = true }
                        }
                    }
                },
                onBackToLogin = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // ---------- HOME ----------
        composable(Rutas.HOME) {
            val registrosHoy = homeVM.obtenerConsumoHoy()

            HomeScreen(
                registrosDeHoy = registrosHoy,
                onOpenCamera = { navController.navigate(Rutas.CAMARA) },
                onOpenRegistro = { navController.navigate(Rutas.REGISTRO) },
                onOpenPerfil = { navController.navigate(Rutas.PERFIL) },
                onOpenIMC = { navController.navigate(Rutas.IMC) },
                onLogout = {
                    authManager.logout()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                }
            )
        }

        // ---------- CÁMARA ----------
        composable(Rutas.CAMARA) {
            CamaraScreen(
                onFotoTomada = { bitmap ->
                    camaraVM.analizarFoto(bitmap)
                    navController.navigate(Rutas.RESULTADO)
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- RESULTADO ANALISIS ----------
        composable(Rutas.RESULTADO) {
            ResultadoAnalisisScreen(
                resultado = camaraVM.resultado.value,
                error = camaraVM.error.value,
                cargando = camaraVM.cargando.value,
                onGuardar = {
                    camaraVM.resultado.value?.let { analisis ->
                        resultadoVM.agregarARegistro(analisis)
                        navController.popBackStack()
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- REGISTRO DIARIO ----------
        composable(Rutas.REGISTRO) {
            val registros = registroDiarioVM.obtenerRegistrosDeHoy()
            RegistroDiarioScreen(
                registros = registros,
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- PERFIL ----------
        composable(Rutas.PERFIL) {
            val usuario = perfilVM.usuario.value
            PerfilScreen(
                usuario = usuario,
                onActualizarPeso = { perfilVM.actualizarPeso(it) },
                onActualizarAltura = { perfilVM.actualizarAltura(it) },
                onActualizarObjetivo = { perfilVM.actualizarObjetivo(it) },
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- IMC ----------
        composable(Rutas.IMC) {
            IMCScreen(
                imc = imcVM.imc.value,
                categoria = imcVM.categoria.value,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
