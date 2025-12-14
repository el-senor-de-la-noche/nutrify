package com.nutrify.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.ui.platform.LocalContext

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.dominio.enums.*
import com.nutrify.ui.components.*
import com.nutrify.ui.viewmodel.PerfilViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory
import kotlin.math.roundToInt

@Composable
fun PerfilScreen(
    onBack: () -> Unit,
    onLogout: ()-> Unit
) {
    val viewModel = viewModel<PerfilViewModel>(
        factory = ViewModelFactory(LocalContext.current)
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPerfilUsuario()
    }

    Scaffold(
        topBar = {
            PerfilTopBar(
                isEditando = state.isEditando,
                onBack = onBack,
                onToggleEditando = viewModel::toggleEditando,
                onGuardarCambios = viewModel::mostrarDialogoConfirmacion
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
                    onRetry = viewModel::cargarPerfilUsuario
                )
            } else {
                PerfilContent(
                    state = state,
                    onNombreChanged = viewModel::onNombreChanged,
                    onFechaNacimientoChanged = viewModel::onFechaNacimientoChanged,
                    onSexoChanged = viewModel::onSexoChanged,
                    onPesoChanged = viewModel::onPesoChanged,
                    onAlturaChanged = viewModel::onAlturaChanged,
                    onNivelActividadChanged = viewModel::onNivelActividadChanged,
                    onObjetivoChanged = viewModel::onObjetivoChanged,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        // Diálogo de confirmación
        if (state.mostrarDialogoConfirmacion) {
            AlertDialog(
                onDismissRequest = viewModel::ocultarDialogoConfirmacion,
                title = { Text("Guardar Cambios") },
                text = { Text("¿Estás seguro de que quieres guardar los cambios en tu perfil?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.guardarCambios()
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::ocultarDialogoConfirmacion) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Notificación de guardado exitoso
        if (state.isGuardado) {
            LaunchedEffect(Unit) {
                // Se ocultará automáticamente después de 2 segundos
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Snackbar(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil actualizado exitosamente")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilTopBar(
    isEditando: Boolean,
    onBack: () -> Unit,
    onToggleEditando: () -> Unit,
    onGuardarCambios: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Mi Perfil",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            if (isEditando) {
                IconButton(
                    onClick = onGuardarCambios,
                    enabled = !false // Aquí deberías verificar si hay cambios pendientes
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar cambios")
                }
            } else {
                IconButton(onClick = onToggleEditando) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar perfil")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilContent(
    state: com.nutrify.ui.viewmodel.PerfilState,
    onNombreChanged: (String) -> Unit,
    onFechaNacimientoChanged: (String) -> Unit,
    onSexoChanged: (Sexo) -> Unit,
    onPesoChanged: (String) -> Unit,
    onAlturaChanged: (String) -> Unit,
    onNivelActividadChanged: (NivelActividad) -> Unit,
    onObjetivoChanged: (ObjetivoNutricional) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Información personal
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información Personal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isEditando) {
                    // Modo edición
                    OutlinedTextField(
                        value = state.nombre,
                        onValueChange = onNombreChanged,
                        label = { Text("Nombre") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { /* No editable */ },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.fechaNacimiento,
                        onValueChange = onFechaNacimientoChanged,
                        label = { Text("Fecha de Nacimiento (AAAA-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selector de sexo
                    Text(
                        text = "Sexo",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Sexo.values().forEach { sexo ->
                            FilterChip(
                                selected = state.sexo == sexo,
                                onClick = { onSexoChanged(sexo) },
                                label = { Text(sexo.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // Modo visualización
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        label = "Nombre",
                        value = state.nombre
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = state.email
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Cake,
                        label = "Fecha de Nacimiento",
                        value = state.fechaNacimiento
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.Transgender,
                        label = "Sexo",
                        value = state.sexo.name
                    )
                }
            }
        }

        // Información física
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información Física",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isEditando) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.peso,
                            onValueChange = onPesoChanged,
                            label = { Text("Peso (kg)") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = state.altura,
                            onValueChange = onAlturaChanged,
                            label = { Text("Altura (m)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileInfoColumn(
                            label = "Peso",
                            value = "${state.peso} kg",
                            icon = Icons.Default.FitnessCenter
                        )

                        ProfileInfoColumn(
                            label = "Altura",
                            value = "${state.altura} m",
                            icon = Icons.Default.Height
                        )
                    }
                }
            }
        }

        // Nivel de actividad y objetivo
        Card(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isEditando) {
                    // Nivel de actividad
                    Text(
                        text = "Nivel de Actividad",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NivelActividad.values().forEach { nivel ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (state.nivelActividad == nivel)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ),
                                onClick = { onNivelActividadChanged(nivel) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = state.nivelActividad == nivel,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(nivel.name)
                                        Text(
                                            text = obtenerDescripcionNivelActividad(nivel),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Objetivo nutricional
                    Text(
                        text = "Objetivo Nutricional",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ObjetivoNutricional.values().forEach { objetivo ->
                            FilterChip(
                                selected = state.objetivo == objetivo,
                                onClick = { onObjetivoChanged(objetivo) },
                                label = { Text(objetivo.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    ProfileInfoRow(
                        icon = Icons.Default.DirectionsRun,
                        label = "Nivel de Actividad",
                        value = state.nivelActividad.name
                    )

                    ProfileInfoRow(
                        icon = Icons.Default.TrackChanges,
                        label = "Objetivo",
                        value = state.objetivo.name
                    )
                }
            }
        }

        // Recomendaciones nutricionales
        val recomendaciones = remember { mutableStateOf<Map<String, Any>?>(null) }

        LaunchedEffect(state.usuario) {
            recomendaciones.value = null // Esto debería llamar a viewModel.calcularRecomendacionesNutricionales()
        }

        recomendaciones.value?.let { recs ->
            Card(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Recomendaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Aquí mostrarías las recomendaciones calculadas
                    Text("Basado en tu perfil, se recomienda...")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileInfoColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
