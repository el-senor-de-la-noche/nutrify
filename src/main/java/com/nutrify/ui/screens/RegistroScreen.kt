package com.nutrify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.dominio.enums.*
import com.nutrify.ui.viewmodel.RegistroViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory
import com.nutrify.ui.viewmodel.NavegacionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistroSuccess: () -> Unit,
    onIrALogin: () -> Unit
)
 {
    val context = LocalContext.current
    val viewModel = viewModel<RegistroViewModel>(
        factory = ViewModelFactory(context)
    )


     val state by viewModel.state.collectAsState()

     LaunchedEffect(state.isRegistroSuccess) {
         if (state.isRegistroSuccess) {
             viewModel.resetRegistroSuccess()
         }
     }

    // Observar si el registro fue exitoso

     Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Crear Cuenta",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        RegistroContent(
            state = state,
            onNombreChanged = viewModel::onNombreChanged,
            onEmailChanged = viewModel::onEmailChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
            onFechaNacimientoChanged = viewModel::onFechaNacimientoChanged,
            onSexoChanged = viewModel::onSexoChanged,
            onPesoChanged = viewModel::onPesoChanged,
            onAlturaChanged = viewModel::onAlturaChanged,
            onNivelActividadChanged = viewModel::onNivelActividadChanged,
            onObjetivoChanged = viewModel::onObjetivoChanged,
            onToggleShowPassword = viewModel::toggleShowPassword,
            onToggleShowConfirmPassword = viewModel::toggleShowConfirmPassword,
            onRegistrar = {
                viewModel.registrar {
                    onRegistroSuccess()
                }
            }
            ,
            onClearError = viewModel::clearError,
            onIrALogin = onIrALogin,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistroContent(
    state: com.nutrify.ui.viewmodel.RegistroState,
    onNombreChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onFechaNacimientoChanged: (String) -> Unit,
    onSexoChanged: (Sexo) -> Unit,
    onPesoChanged: (String) -> Unit,
    onAlturaChanged: (String) -> Unit,
    onNivelActividadChanged: (NivelActividad) -> Unit,
    onObjetivoChanged: (ObjetivoNutricional) -> Unit,
    onToggleShowPassword: () -> Unit,
    onToggleShowConfirmPassword: () -> Unit,
    onRegistrar: () -> Unit,
    onClearError: () -> Unit,
    onIrALogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Información personal
        Text(
            text = "Información Personal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.nombre,
            onValueChange = onNombreChanged,
            label = { Text("Nombre Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChanged,
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = onToggleShowPassword) {
                        Icon(
                            imageVector = if (state.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                label = { Text("Confirmar") },
                trailingIcon = {
                    IconButton(onClick = onToggleShowConfirmPassword) {
                        Icon(
                            imageVector = if (state.showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (state.showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.fechaNacimiento,
            onValueChange = onFechaNacimientoChanged,
            label = { Text("Fecha de Nacimiento (DD/MM/AAAA)") },
            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Información física
        Text(
            text = "Información Física",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de sexo
        Text(
            text = "Sexo",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.altura,
                onValueChange = onAlturaChanged,
                label = { Text("Altura (m)") },
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nivel de actividad
        Text(
            text = "Nivel de Actividad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = state.nivelActividad == nivel,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = nivel.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = obtenerDescripcionNivelActividad(nivel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Objetivo nutricional
        Text(
            text = "Objetivo Nutricional",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ObjetivoNutricional.values().forEach { objetivo ->
                AssistChip(
                    onClick = { onObjetivoChanged(objetivo) },
                    label = { Text(objetivo.name) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.objetivo == objetivo)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de registro
        Button(
            onClick = onRegistrar,
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
                Text(
                    text = "Registrarse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace a login
        TextButton(onClick = onIrALogin) {
            Text("¿Ya tienes cuenta? Inicia Sesión")
        }


        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun obtenerDescripcionNivelActividad(nivel: NivelActividad): String {
    return when (nivel) {
        NivelActividad.SEDENTARIO -> "Poco o ningún ejercicio"
        NivelActividad.LIGERO -> "Ejercicio ligero 1-3 días/semana"
        NivelActividad.MODERADO -> "Ejercicio moderado 3-5 días/semana"
        NivelActividad.INTENSO -> "Ejercicio intenso 6-7 días/semana"
        NivelActividad.MUY_INTENSO -> "Ejercicio muy intenso y trabajo físico"
    }
}
