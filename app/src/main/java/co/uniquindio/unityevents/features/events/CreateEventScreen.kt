package co.uniquindio.unityevents.features.events

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.uniquindio.unityevents.core.utils.Formatters
import co.uniquindio.unityevents.core.utils.LocationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * Pantalla de creacion de evento. Incluye seleccion de imagen, fecha/hora y todos los
 * campos requeridos. Al enviar, el evento se crea con status PENDING y queda a la espera
 * de aprobacion por un moderador.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CreateEventScreen(
    onBack: () -> Unit,
    onCreated: (eventId: String) -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navegacion a detalle al crearse.
    LaunchedEffect(state.createdEventId) {
        state.createdEventId?.let {
            viewModel.onCreatedConsumed()
            onCreated(it)
        }
    }
    // Muestra snackbar con errores.
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorConsumed()
        }
    }

    // Launcher del Photo Picker del sistema (no requiere permisos).
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> viewModel.onImagePicked(uri) }

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Selector de imagen.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Imagen del evento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Image, null, tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp))
                        Text("Toca para elegir una imagen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.title, onValueChange = viewModel::onTitleChange,
                label = { Text("Titulo") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.description, onValueChange = viewModel::onDescriptionChange,
                label = { Text("Descripcion") }, minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                supportingText = { Text("Minimo 20 caracteres.") }
            )
            Spacer(Modifier.height(12.dp))

            // Chips de categoria.
            Text("Categoria", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CreateEventUiState.CATEGORIES.forEach { cat ->
                    AssistChip(
                        onClick = { viewModel.onCategoryChange(cat) },
                        label = { Text(cat) },
                        colors = if (state.category == cat) {
                            AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else AssistChipDefaults.assistChipColors()
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.placeName, onValueChange = viewModel::onPlaceChange,
                label = { Text("Nombre del lugar") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.address, onValueChange = viewModel::onAddressChange,
                label = { Text("Direccion (opcional)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(16.dp))

            // --- Picker de ubicacion (mapa + boton GPS) ---
            LocationPicker(
                lat = state.latitude,
                lng = state.longitude,
                onLocationPicked = viewModel::onLocationPicked
            )
            Spacer(Modifier.height(12.dp))

            // Selector de fecha + hora.
            OutlinedTextField(
                value = state.startDate?.let { Formatters.formatEventDate(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha y hora") },
                trailingIcon = {
                    IconButton(onClick = { showDateDialog = true }) {
                        Icon(Icons.Filled.CalendarToday, null)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { showDateDialog = true },
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.price, onValueChange = viewModel::onPriceChange,
                    label = { Text("Precio (COP)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                )
                OutlinedTextField(
                    value = state.capacity, onValueChange = viewModel::onCapacityChange,
                    label = { Text("Capacidad") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::onSubmit,
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Publicar evento", style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    // --- Dialogo de fecha ---
    if (showDateDialog) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    pickedDateMillis = dateState.selectedDateMillis
                    showDateDialog = false
                    showTimeDialog = true
                }) { Text("Siguiente") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // --- Dialogo de hora ---
    if (showTimeDialog) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text("Hora del evento") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val dateMillis = pickedDateMillis
                    if (dateMillis != null) {
                        val cal = Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                            set(Calendar.SECOND, 0)
                        }
                        viewModel.onStartDateChange(Date(cal.timeInMillis))
                    }
                    showTimeDialog = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

/**
 * Picker de ubicacion del evento:
 *
 * - Mapa clickeable: al tocar, coloca un marcador en ese punto y actualiza lat/lng.
 * - Boton "Usar mi ubicacion": pide permiso de GPS y centra el marcador en la posicion actual.
 *
 * Posicion por defecto: Armenia (Quindio), Colombia.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun LocationPicker(
    lat: Double?,
    lng: Double?,
    onLocationPicked: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locationHelper = remember { LocationHelper() }
    val permission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    val defaultLatLng = LatLng(4.5339, -75.6811)
    val currentLatLng = if (lat != null && lng != null) LatLng(lat, lng) else null
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLatLng ?: defaultLatLng, 14f)
    }

    // Cuando el estado externo cambia (p.ej. GPS acaba de darnos la ubicacion), recentramos el mapa.
    LaunchedEffect(lat, lng) {
        if (currentLatLng != null) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
        }
    }

    Column {
        Text("Ubicacion del evento *", style = MaterialTheme.typography.titleSmall)
        Text(
            "Toca el mapa para marcar el punto exacto, o usa tu ubicacion actual.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permission.status.isGranted),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false, // zoom con gestos / scroll / Ctrl+drag
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true
                ),
                onMapClick = { latLng -> onLocationPicked(latLng.latitude, latLng.longitude) }
            ) {
                currentLatLng?.let { pos ->
                    Marker(
                        state = MarkerState(position = pos),
                        title = "Ubicacion del evento"
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                if (permission.status.isGranted) {
                    scope.launch {
                        locationHelper.getCurrentLocation(context)?.let { coords ->
                            onLocationPicked(coords.lat, coords.lng)
                        }
                    }
                } else {
                    permission.launchPermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.MyLocation, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                if (permission.status.isGranted) "Usar mi ubicacion actual"
                else "Permitir acceso a mi ubicacion"
            )
        }

        if (currentLatLng != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Ubicacion marcada: ${"%.5f".format(lat)}, ${"%.5f".format(lng)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

