package com.nutrify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nutrify.ui.screens.*
import com.nutrify.ui.theme.NutrifyTheme
import com.nutrify.ui.viewmodel.Destino
import com.nutrify.ui.viewmodel.NavegacionViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutrifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NutrifyApp()
                }
            }
        }
    }
}

@Composable
fun NutrifyApp() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val navegacionViewModel: NavegacionViewModel = viewModel(
        factory = ViewModelFactory(context)
    )

    val state by navegacionViewModel.state.collectAsState()

    /**
     * ✅ CONTROL SOLO DE AUTENTICACIÓN (NO forzar destinoActual)
     * Si NO está autenticado, lo mandamos a Login limpiando el backstack.
     */
    LaunchedEffect(state.estaAutenticado) {
        if (!state.estaAutenticado) {
            navController.navigate(Destino.Login.ruta) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // Si está autenticado y quedó en Login (ej: app recién abierta), lo mandamos a Home
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == Destino.Login.ruta) {
                navController.navigate(Destino.Home.ruta) {
                    popUpTo(Destino.Login.ruta) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // ✅ Start destination dinámico (evita iniciar en Login si ya hay sesión)
    val startDestination = if (state.estaAutenticado) Destino.Home.ruta else Destino.Login.ruta

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Destino.Login.ruta) {
            LoginScreen(
                onLoginSuccess = {
                    navegacionViewModel.onLoginExitoso()

                    navController.navigate(Destino.Home.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegistro = {
                    navController.navigate(Destino.Registro.ruta) {
                        launchSingleTop = true
                    }
                },
                onEntrarInvitado = {
                    // ✅ No hace falta navegar aquí: el LoginViewModel marcará isLoginSuccess
                    // y tu LaunchedEffect(state.isLoginSuccess) hará onLoginSuccess().
                    // Si quieres navegación inmediata, descomenta:
                    navegacionViewModel.onLoginExitoso()
                    navController.navigate(Destino.Home.ruta) {
                        popUpTo(Destino.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destino.Registro.ruta) {
            RegistroScreen(
                onRegistroSuccess = {
                    // Volver a Login sin duplicar pantallas
                    navController.popBackStack()
                },
                onIrALogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Destino.Home.ruta) {
            HomeScreen(
                onNavigateToCamara = {
                    navController.navigate(Destino.Camara.ruta) { launchSingleTop = true }
                },
                onNavigateToRegistroDiario = {
                    navController.navigate(Destino.RegistroDiario.ruta) { launchSingleTop = true }
                },
                onNavigateToPerfil = {
                    navController.navigate(Destino.Perfil.ruta) { launchSingleTop = true }
                },
                onNavigateToIMC = {
                    navController.navigate(Destino.IMC.ruta) { launchSingleTop = true }
                }
            )
        }

        composable(Destino.Camara.ruta) {
            CameraScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destino.RegistroDiario.ruta) {
            RegistroDiarioScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destino.Perfil.ruta) {
            PerfilScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    // ✅ Al cerrar sesión, limpias stack y vuelves a Login
                    navController.navigate(Destino.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        composable(Destino.IMC.ruta) {
            IMCScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
