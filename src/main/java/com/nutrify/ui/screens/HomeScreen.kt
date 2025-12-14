package com.nutrify.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.ui.components.*
import com.nutrify.ui.viewmodel.HomeViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory
import kotlin.math.roundToInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun HomeScreen(
    onNavigateToCamara: () -> Unit,
    onNavigateToRegistroDiario: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToIMC: () -> Unit
) {
    val viewModel = viewModel<HomeViewModel>(
        factory = ViewModelFactory(LocalContext.current)
    )
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.cargarRegistrosHoy()
                viewModel.cargarUsuarioActual()
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }


    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                userName = state.userName,
                onNavigateToPerfil = onNavigateToPerfil
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCamara,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp) // Para no tapar el bottom navigation
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Analizar comida")
            }
        },
        bottomBar = {
            HomeBottomNavigation(
                currentScreen = "home",
                onNavigateToHome = { /* Ya estamos en home */ },
                onNavigateToRegistroDiario = onNavigateToRegistroDiario,
                onNavigateToCamara = onNavigateToCamara,
                onNavigateToPerfil = onNavigateToPerfil,
                onNavigateToIMC = onNavigateToIMC
            )
        }
    ) { paddingValues ->
        Crossfade(
            targetState = state.isLoading,
            label = "loadingTransition"
        ) { isLoading ->
            if (isLoading) {
                LoadingScreen()
            } else if (state.errorMessage != null) {
                ErrorScreen(
                    message = state.errorMessage ?: "Error desconocido",
                    onRetry = viewModel::cargarRegistrosHoy
                )
            } else {
                HomeContent(
                    state = state,
                    viewModel = viewModel,
                    onNavigateToRegistroDiario = onNavigateToRegistroDiario,
                    onNavigateToIMC = onNavigateToIMC,
                    onEliminarRegistro = viewModel::eliminarRegistro,
                    onToggleMostrarResumen = viewModel::toggleMostrarResumen,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    userName: String,
    onNavigateToPerfil: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bienvenido",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (userName.isNotBlank()) userName else "Nutrify",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToPerfil) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp)
                )
            }
        }
    )
}

@Composable
private fun HomeBottomNavigation(
    currentScreen: String,
    onNavigateToHome: () -> Unit,
    onNavigateToRegistroDiario: () -> Unit,
    onNavigateToCamara: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToIMC: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = currentScreen == "home",
            onClick = onNavigateToHome
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Registros") },
            label = { Text("Registros") },
            selected = currentScreen == "registros",
            onClick = onNavigateToRegistroDiario
        )

        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Analizar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            label = { Text("Analizar") },
            selected = false,
            onClick = onNavigateToCamara
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = currentScreen == "perfil",
            onClick = onNavigateToPerfil
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.MonitorWeight, contentDescription = "IMC") },
            label = { Text("IMC") },
            selected = currentScreen == "imc",
            onClick = onNavigateToIMC
        )
    }
}

@Composable
private fun HomeContent(
    state: com.nutrify.ui.viewmodel.HomeState,
    viewModel: HomeViewModel,
    onNavigateToRegistroDiario: () -> Unit,
    onNavigateToIMC: () -> Unit,
    onEliminarRegistro: (String) -> Unit,
    onToggleMostrarResumen: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Resumen diario
            DailySummaryCard(
                totales = state.totalesHoy,
                objetivoCalorias = state.objetivoCalorias,
                porcentajeCalorias = state.porcentajeCalorias,
                mostrarResumen = state.mostrarResumen,
                onToggleMostrarResumen = onToggleMostrarResumen,
                onVerDetalles = onNavigateToRegistroDiario
            )
        }

        item {
            // Quick stats
            QuickStatsCard(
                stats = viewModel.obtenerEstadisticasRapidas(),
                onNavigateToIMC = onNavigateToIMC
            )
        }

        item {
            // Título de registros recientes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registros Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onNavigateToRegistroDiario) {
                    Text("Ver todos")
                }
            }
        }

        if (state.registrosHoy.isEmpty()) {
            item {
                EmptyState(
                    title = "No hay registros hoy",
                    message = "Toca el botón + para analizar tu primera comida",
                    icon = Icons.Default.Restaurant,
                    action = {
                        // El botón FAB ya está disponible
                    }
                )
            }
        } else {
            items(state.registrosHoy) { registro ->
                FoodRecordCard(
                    registro = registro,
                    onDelete = { onEliminarRegistro(registro.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Espacio para el FAB
        }
    }
}

@Composable
private fun DailySummaryCard(
    totales: com.nutrify.dominio.modelos.TotalesDiarios,
    objetivoCalorias: Double,
    porcentajeCalorias: Double,
    mostrarResumen: Boolean,
    onToggleMostrarResumen: () -> Unit,
    onVerDetalles: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resumen del Día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onToggleMostrarResumen) {
                    Icon(
                        imageVector = if (mostrarResumen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (mostrarResumen) "Ocultar resumen" else "Mostrar resumen"
                    )
                }
            }

            if (mostrarResumen) {
                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progreso de calorías
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Calorías",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${totales.calorias.roundToInt()} / ${objetivoCalorias.roundToInt()} kcal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress =  (porcentajeCalorias / 100).toFloat() ,
                        modifier = Modifier.fillMaxWidth(),
                        color = when {
                            porcentajeCalorias > 100 -> MaterialTheme.colorScheme.error
                            porcentajeCalorias > 80 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${porcentajeCalorias.roundToInt()}% del objetivo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Macronutrientes
                NutritionInfoRow(
                    calories = totales.calorias,
                    protein = totales.proteinas,
                    fiber = totales.fibra
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para ver detalles
                OutlinedButton(
                    onClick = onVerDetalles,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Ver Detalles Completos")
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    stats: Map<String, Any>,
    onNavigateToIMC: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estadísticas Rápidas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AssistChip(
                    onClick = onNavigateToIMC,
                    label = { Text("IMC") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.MonitorWeight,
                            contentDescription = "Calcular IMC",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.forEach { (key, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (key) {
                                "registros_hoy" -> "Registros"
                                "calorias_consumidas" -> "Calorías"
                                "calorias_restantes" -> "Restantes"
                                "proteinas_consumidas" -> "Proteínas"
                                "fibra_consumida" -> "Fibra"
                                else -> key.replace("_", " ").replaceFirstChar{
                                    if(it.isLowerCase()) it.titlecase() else it.toString()
                                }
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                value is Double -> value.roundToInt().toString()
                                else -> value.toString()
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
