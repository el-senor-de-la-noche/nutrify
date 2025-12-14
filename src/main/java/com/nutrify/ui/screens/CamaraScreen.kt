package com.nutrify.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nutrify.ui.viewmodel.CamaraViewModel
import com.nutrify.ui.viewmodel.ViewModelFactory
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel: CamaraViewModel = viewModel(factory = ViewModelFactory(context))
    val state by viewModel.state.collectAsState()

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            viewModel.limpiarError()
        }
    }

    // ✅ runtime permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Analizar comida") },
                actions = {
                    if (state.imagenCapturada != null) {
                        IconButton(onClick = viewModel::reiniciar) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when {
                !hasPermission -> {
                    // No FAB si no hay permiso
                }

                state.imagenCapturada == null -> {
                    FloatingActionButton(
                        onClick = {
                            val capture = imageCapture ?: return@FloatingActionButton

                            val file = File(
                                context.cacheDir,
                                "nutrify_capture_${System.currentTimeMillis()}.jpg"
                            )

                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                            capture.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        try {
                                            val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                            val rotated = rotateBitmapIfNeeded(bmp, file.absolutePath)
                                            viewModel.onImagenCapturada(rotated)
                                        } catch (e: Exception) {
                                            // manda error al VM
                                            // (si quieres, puedes agregar un método setError)
                                            // por ahora reutilizamos errorMessage:
                                            // viewModel.setError("Error al capturar: ${e.message}")
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        // viewModel.setError("Error de cámara: ${exception.message}")
                                    }
                                }
                            )
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                    }
                }

                state.imagenCapturada != null && !state.mostrarResultados -> {
                    FloatingActionButton(onClick = viewModel::analizarImagen) {
                        if (state.estaProcesando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasPermission -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("Necesitamos permiso de cámara para analizar tu comida.")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Conceder permiso")
                        }
                    }
                }

                state.estaProcesando -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Analizando imagen…")
                        Text("Esto puede tomar unos segundos", style = MaterialTheme.typography.bodySmall)
                    }
                }

                state.mostrarResultados -> {
                    val analisis = state.analisis
                    if (analisis != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            state.imagenCapturada?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                )
                            }

                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "🍽️ ${analisis.descripcion ?: "Comida analizada"}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    Row(Modifier.fillMaxWidth()) {
                                        Text("🔥 Calorías:", Modifier.weight(1f))
                                        Text("${analisis.calorias} kcal")
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    Row(Modifier.fillMaxWidth()) {
                                        Text("🥩 Proteínas:", Modifier.weight(1f))
                                        Text("${analisis.proteinas} g")
                                    }
                                    Spacer(Modifier.height(8.dp))

                                    Row(Modifier.fillMaxWidth()) {
                                        Text("🌾 Fibra:", Modifier.weight(1f))
                                        Text("${analisis.fibra} g")
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val ok = viewModel.guardarAnalisisEnRegistro(tipoComida = "Comida")
                                    if (ok) onBack()
                                    // si falla, ya te deja errorMessage y snackbar
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar y volver")
                            }

                        }
                    }
                }

                else -> {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(),
                        onImageCaptureReady = { imageCapture = it }
                    )
                }
            }
        }
    }
}

/* ============================= */
/* CAMERA PREVIEW                */
/* ============================= */

@Composable
private fun CameraPreview(
    modifier: Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        modifier = modifier,
        factory = {
            val previewView = PreviewView(it)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(it)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    // ayuda a que la rotación sea correcta
                    .setTargetRotation(previewView.display.rotation)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )

                onImageCaptureReady(imageCapture)
            }, ContextCompat.getMainExecutor(it))

            previewView
        }
    )
}

/* ============================= */
/* ROTACIÓN CON EXIF             */
/* ============================= */

private fun rotateBitmapIfNeeded(bitmap: Bitmap, filePath: String): Bitmap {
    val exif = ExifInterface(filePath)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val rotationDegrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }

    if (rotationDegrees == 0f) return bitmap

    val matrix = Matrix().apply { postRotate(rotationDegrees) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
