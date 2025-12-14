package com.nutrify.ui.screens


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.ui.components.CircularProgress
import com.nutrify.ui.components.EmptyState
import com.nutrify.ui.components.ErrorScreen
import com.nutrify.ui.components.*
import com.nutrify.ui.viewmodel.*
import java.time.LocalDate

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun RegistroDiarioScreen(
    onBack: () -> Unit
) {
    val viewModel: RegistroDiarioViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            RegistroDiarioTopBar(
                fechaSeleccionada = state.fechaSeleccionada,
                onBack = onBack,
                onCambiarFecha = viewModel::cambiarFecha
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
                    onRetry = { viewModel.cargarRegistrosHoy() }
                )
            } else if (state.registros.isEmpty()) {
                EmptyState(
                    title = "No hay registros",
                    message = "No hay registros de comida para esta fecha",
                    icon = Icons.Default.Restaurant,
                    action = {
                        // El botón para agregar estaría en otra pantalla
                    }
                )
            } else {
                RegistroDiarioContent(
                    state = state,
                    onEliminarRegistro = viewModel::mostrarDialogoEliminar,
                    onAplicarFiltro = viewModel::aplicarFiltroTipoComida,
                    onLimpiarFiltro = viewModel::limpiarFiltro,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Diálogo de confirmación para eliminar
        if (state.mostrarDialogoEliminar) {
            AlertDialog(
                onDismissRequest = viewModel::ocultarDialogoEliminar,
                title = { Text("Eliminar Registro") },
                text = { Text("¿Estás seguro de que quieres eliminar este registro? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.eliminarRegistro()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::ocultarDialogoEliminar) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistroDiarioTopBar(
    fechaSeleccionada: LocalDate,
    onBack: () -> Unit,
    onCambiarFecha: (LocalDate) -> Unit
) {
    var mostrarSelectorFecha by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Registro Diario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatearFecha(fechaSeleccionada),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = { mostrarSelectorFecha = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Cambiar fecha")
            }
        }
    )

    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            onDateSelected = { fecha ->
                onCambiarFecha(fecha)
                mostrarSelectorFecha = false
            },
            initialDate = fechaSeleccionada
        )
    }
}

@Composable
private fun RegistroDiarioContent(
    state: com.nutrify.ui.viewmodel.RegistroDiarioState,
    onEliminarRegistro: (com.nutrify.dominio.modelos.RegistroAlimento) -> Unit,
    onAplicarFiltro: (String?) -> Unit,
    onLimpiarFiltro: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Resumen del día
            DailyTotalCard(totales = state.totales)
        }

        item {
            // Filtros
            if (state.tiposComidaDisponibles.isNotEmpty()) {
                FilterSection(
                    tiposComida = state.tiposComidaDisponibles,
                    filtroActual = state.filtroTipoComida,
                    onAplicarFiltro = onAplicarFiltro,
                    onLimpiarFiltro = onLimpiarFiltro
                )
            }
        }

        item {
            // Título de registros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${state.registros.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val registrosFiltrados = if (state.filtroTipoComida != null) {
            state.registros.filter { it.tipoComida == state.filtroTipoComida }
        } else {
            state.registros
        }

        if (registrosFiltrados.isEmpty()) {
            item {
                EmptyState(
                    title = "No hay registros con este filtro",
                    message = "No hay registros de comida para el tipo seleccionado",
                    icon = Icons.Default.FilterAlt
                )
            }
        } else {
            items(registrosFiltrados) { registro ->
                FoodRecordCard(
                    registro = registro,
                    onDelete = { onEliminarRegistro(registro) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun DailyTotalCard(
    totales: com.nutrify.dominio.modelos.TotalesDiarios
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
            Text(
                text = "Total del Día",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            NutritionInfoRow(
                calories = totales.calorias,
                protein = totales.proteinas,
                fiber = totales.fibra
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progreso circular para calorías
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    CircularProgress(
                        progress = 0.75f, // Esto debería venir del ViewModel
                        modifier = Modifier.size(120.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${totales.calorias.toInt()}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    tiposComida: List<String>,
    filtroActual: String?,
    onAplicarFiltro: (String?) -> Unit,
    onLimpiarFiltro: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Filtrar por tipo",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                if (filtroActual != null) {
                    TextButton(onClick = onLimpiarFiltro) {
                        Text("Limpiar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tiposComida.forEach { tipo ->
                    FilterChip(
                        selected = filtroActual == tipo,
                        onClick = {
                            if (filtroActual == tipo) {
                                onLimpiarFiltro()
                            } else {
                                onAplicarFiltro(tipo)
                            }
                        },
                        label = { Text(tipo) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatearFecha(fecha: LocalDate): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    return fecha.format(formatter)
}

// DatePickerDialog simplificado (en una app real usarías un DatePicker de Material 3)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Seleccionar Fecha") },
        text = {
            // Aquí iría un DatePicker real
            Text("Selector de fecha (implementación simplificada)")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // En una implementación real, aquí obtendrías la fecha seleccionada
                    onDateSelected(LocalDate.now())
                }
            ) {
                Text("Seleccionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
