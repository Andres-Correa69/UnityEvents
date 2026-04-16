package co.uniquindio.unityevents.features.tickets

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.core.utils.Formatters
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Escaner de QR usado por moderadores para validar tickets en la entrada del evento.
 *
 * Pide permiso de camara via Accompanist; al aceptar, renderiza un `PreviewView` de CameraX
 * y conecta un `ImageAnalysis` con el detector de ML Kit para codigos QR.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onBack: () -> Unit,
    viewModel: QrScannerViewModel = hiltViewModel()
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !cameraPermission.status.isGranted -> PermissionRequestContent(
                    rationale = cameraPermission.status.shouldShowRationale,
                    onRequest = { cameraPermission.launchPermissionRequest() }
                )
                else -> {
                    CameraPreview(onPayloadDetected = viewModel::onScanned)
                    ScannerOverlay()
                }
            }

            // Dialogo de resultado.
            state.lastScannedTicket?.let { ticket ->
                AlertDialog(
                    onDismissRequest = viewModel::onDismissResult,
                    confirmButton = {
                        TextButton(onClick = viewModel::onDismissResult) { Text("Continuar") }
                    },
                    icon = {
                        Icon(
                            imageVector = if (ticket.isUsed && !state.wasAlreadyUsed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    },
                    title = { Text("Ticket ${if (state.wasAlreadyUsed) "ya usado" else "validado"}") },
                    text = {
                        Column {
                            Text(ticket.eventTitle)
                            Text("Asistente: ${ticket.userName}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Fecha: ${Formatters.formatEventDate(ticket.eventStartDate)}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                )
            }
            state.errorMessage?.let { msg ->
                AlertDialog(
                    onDismissRequest = viewModel::onDismissResult,
                    confirmButton = {
                        TextButton(onClick = viewModel::onDismissResult) { Text("Reintentar") }
                    },
                    icon = { Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("QR no valido") },
                    text = { Text(msg) }
                )
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(rationale: Boolean, onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.QrCodeScanner, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Permiso de camara requerido",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            if (rationale)
                "La camara es necesaria para leer el QR de los tickets. Acepta el permiso para continuar."
            else
                "Para validar los QR de los asistentes necesitamos acceso a la camara.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) { Text("Otorgar permiso") }
    }
}

/**
 * Preview de camara + analisis de frames con ML Kit. Cada QR detectado dispara [onPayloadDetected].
 */
@Composable
private fun CameraPreview(onPayloadDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(1280, 720))
                    .build()
                    .also { it.setAnalyzer(executor) { proxy -> processImage(proxy, barcodeScanner, onPayloadDetected) } }

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                }
            }, androidx.core.content.ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Extrae el primer QR del frame y lo reporta. Cierra el proxy al terminar.
 * OptIn por el uso de [ImageProxy.image] (API experimental).
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImage(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onPayloadDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close(); return
    }
    val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(input)
        .addOnSuccessListener { barcodes ->
            val payload = barcodes.firstOrNull()?.rawValue
            if (!payload.isNullOrBlank()) onPayloadDetected(payload)
        }
        .addOnCompleteListener { imageProxy.close() }
}

/** Marco guia superpuesto sobre el preview, estilo escaner. */
@Composable
private fun ScannerOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.12f))
        )
        Text(
            "Alinea el codigo QR dentro del marco",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
