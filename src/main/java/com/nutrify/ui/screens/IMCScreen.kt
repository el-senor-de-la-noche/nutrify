package com.nutrify.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.ui.components.*
import com.nutrify.ui.viewmodel.IMCViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IMCScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: IMCViewModel = viewModel(factory = ViewModelFactory(context))
    val state by viewModel.state.collectAsState()

    /**
     * ✅ Fix principal:
     * Cada vez que esta pantalla vuelve a primer plano (ON_RESUME),
     * si "usarDatosPerfil" está activo, recargamos perfil.
     * Esto hace que tome cambios hechos en Perfil (peso/altura) sin quedarse con defaults.
     */
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state.usarDatosPerfil) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && state.usarDatosPerfil) {
                viewModel.cargarDatosPerfil()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    /**
     * ✅ Primer render:
     * Si usarDatosPerfil está activo, cargamos una vez.
     */
    LaunchedEffect(Unit) {
        if (state.usarDatosPerfil) {
            viewModel.cargarDatosPerfil()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Calculadora IMC",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                    onRetry = { viewModel.calcularIMC() }
                )
            } else {
                IMCContent(
                    state = state,
                    onPesoChanged = viewModel::onPesoChanged,
                    onAlturaChanged = viewModel::onAlturaChanged,
                    onToggleUsarDatosPerfil = {
                        // ✅ esto ya lo maneja el ViewModel,
                        // pero si se activa, forzamos recarga inmediata.
                        viewModel.toggleUsarDatosPerfil()
                        if (!state.usarDatosPerfil) {
                            // Si venía desactivado y lo activaron, recargar ya.
                            viewModel.cargarDatosPerfil()
                        }
                    },
                    onCalcularIMC = viewModel::calcularIMC,
                    onReiniciarCalculo = viewModel::reiniciarCalculo,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun IMCContent(
    state: com.nutrify.ui.viewmodel.IMCState,
    onPesoChanged: (String) -> Unit,
    onAlturaChanged: (String) -> Unit,
    onToggleUsarDatosPerfil: () -> Unit,
    onCalcularIMC: () -> Unit,
    onReiniciarCalculo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector de fuente de datos
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.usarDatosPerfil,
                    onCheckedChange = { onToggleUsarDatosPerfil() }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Usar datos de mi perfil",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Utilizar peso y altura de tu perfil",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Campos de entrada
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Datos para el cálculo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.peso,
                        onValueChange = onPesoChanged,
                        label = { Text("Peso (kg)") },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                        enabled = !state.usarDatosPerfil,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = state.altura,
                        onValueChange = onAlturaChanged,
                        label = { Text("Altura (m)") },
                        leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                        enabled = !state.usarDatosPerfil,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onCalcularIMC,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calcular IMC")
                    }
                }
            }
        }

        // Resultados
        if (state.imcCalculado > 0) {
            ResultadosIMC(
                state = state,
                onReiniciarCalculo = onReiniciarCalculo
            )
        } else {
            // Información sobre IMC
            Card(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¿Qué es el IMC?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "El Índice de Masa Corporal (IMC) es una medida que relaciona tu peso con tu altura. Es un indicador útil para evaluar si tu peso está dentro de un rango saludable.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Clasificación IMC",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IMCTable()
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultadosIMC(
    state: com.nutrify.ui.viewmodel.IMCState,
    onReiniciarCalculo: () -> Unit
) {
    // ✅ Control local para abrir/cerrar el diálogo
    var showDialog by remember(state.imcCalculado, state.diferenciaPeso) {
        mutableStateOf(state.diferenciaPeso != 0.0 && state.imcCalculado > 0.0)
    }

    Card(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resultados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Valor del IMC
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tu IMC es",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f", state.imcCalculado),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = obtenerColorIMC(state.imcCalculado)
                    )
                }

                Badge(
                    containerColor = obtenerColorIMC(state.imcCalculado).copy(alpha = 0.2f),
                    contentColor = obtenerColorIMC(state.imcCalculado)
                ) {
                    Text(
                        state.categoriaIMC,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Riesgo para la salud
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Riesgo para la salud",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = state.riesgoSalud,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peso ideal
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Rango de peso saludable",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${String.format("%.1f", state.pesoIdealMin)} - ${String.format("%.1f", state.pesoIdealMax)} kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ✅ Diálogo cerrable (arreglado)
            if (showDialog && state.diferenciaPeso != 0.0) {
                val mensajeDiferencia = if (state.diferenciaPeso > 0) {
                    "Necesitas perder ${String.format("%.1f", state.diferenciaPeso)} kg para alcanzar un peso saludable"
                } else {
                    "Necesitas ganar ${String.format("%.1f", -state.diferenciaPeso)} kg para alcanzar un peso saludable"
                }

                AlertDialog(
                    onDismissRequest = { showDialog = false }, // ✅ ahora se puede cerrar tocando afuera
                    title = { Text("Recomendación") },
                    text = { Text(mensajeDiferencia) },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) { // ✅ ahora se puede cerrar con botón
                            Text("Entendido")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recomendaciones
            val recomendaciones = remember { mutableStateOf<List<String>>(emptyList()) }

            LaunchedEffect(state.imcCalculado) {
                recomendaciones.value = listOf(
                    "Consulta con un profesional de la salud",
                    "Mantén una dieta equilibrada",
                    "Realiza actividad física regular"
                )
            }

            if (recomendaciones.value.isNotEmpty()) {
                Column {
                    Text(
                        text = "Recomendaciones",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    recomendaciones.value.forEach { recomendacion ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recomendacion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para nuevo cálculo
            OutlinedButton(
                onClick = {
                    showDialog = false
                    onReiniciarCalculo()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Cálculo")
            }
        }
    }
}

@Composable
private fun IMCTable() {
    val clasificaciones = listOf(
        Triple("Bajo peso", "< 18.5", Color.Blue),
        Triple("Normal", "18.5 - 24.9", Color.Green),
        Triple("Sobrepeso", "25.0 - 29.9", Color.Yellow),
        Triple("Obesidad", "≥ 30.0", Color.Red)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        clasificaciones.forEach { (categoria, rango, color) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, androidx.compose.foundation.shape.CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = categoria,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    text = rango,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun obtenerColorIMC(imc: Double): Color {
    return when {
        imc < 18.5 -> Color.Blue
        imc < 25 -> Color.Green
        imc < 30 -> Color.Yellow
        else -> Color.Red
    }
}
