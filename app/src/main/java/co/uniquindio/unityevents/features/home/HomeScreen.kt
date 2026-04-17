package co.uniquindio.unityevents.features.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.core.component.EmptyState
import co.uniquindio.unityevents.core.component.EventCard
import co.uniquindio.unityevents.core.component.LoadingBox
import co.uniquindio.unityevents.core.utils.LocationHelper
import co.uniquindio.unityevents.domain.model.Event
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/** Posicion por defecto del mapa — centrado en Armenia (Quindio), Colombia. */
private val ARMENIA_QUINDIO = LatLng(4.5339, -75.6811)

/**
 * Pantalla principal: mapa con los eventos ubicados geograficamente.
 *
 * - Marcadores violeta para eventos aprobados (publicos).
 * - Marcadores naranja para eventos propios pendientes / rechazados (solo los ve el creador).
 * - Busqueda + filtros que filtran los marcadores en vivo.
 * - Bottom card con preview del evento al tocar un marcador.
 * - FAB "Crear evento".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onEventClick: (eventId: String) -> Unit,
    onCreateEventClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Permiso de ubicacion (opcional; la app funciona sin el).
    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val locationHelper = remember { LocationHelper() }

    // Estado de la camara del mapa.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ARMENIA_QUINDIO, 13f)
    }

    // Al iniciar, si tenemos permiso, centramos en la ubicacion actual.
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            scope.launch {
                locationHelper.getCurrentLocation(context)?.let { coords ->
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(LatLng(coords.lat, coords.lng), 14f)
                }
            }
        } else {
            // Pide el permiso una sola vez al entrar.
            locationPermission.launchPermissionRequest()
        }
    }

    // Evento actualmente seleccionado en el mapa (muestra la tarjeta inferior).
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    // Solo eventos con coordenadas validas aparecen en el mapa.
    val eventsOnMap = state.visibleEvents.filter { it.latitude != null && it.longitude != null }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Crear evento") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // --- Mapa de eventos ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = locationPermission.status.isGranted
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false, // zoom con gestos / scroll / Ctrl+drag
                    myLocationButtonEnabled = locationPermission.status.isGranted,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true
                ),
                onMapClick = { selectedEvent = null }
            ) {
                eventsOnMap.forEach { event ->
                    val hue = if (event.status == co.uniquindio.unityevents.domain.model.EventStatus.APPROVED)
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_VIOLET
                    else
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE
                    Marker(
                        state = MarkerState(position = LatLng(event.latitude!!, event.longitude!!)),
                        title = event.title,
                        snippet = event.placeName,
                        icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            selectedEvent = event
                            false // permite el comportamiento por defecto (centrar camara).
                        }
                    )
                }
            }

            // --- Overlay superior: buscador + filtros ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchChange,
                    placeholder = { Text("Buscar eventos...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchChange("") }) {
                                Icon(Icons.Filled.Clear, null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                FilterChipsRow(
                    selected = state.filter,
                    onSelect = viewModel::onFilterChange
                )
            }

            // --- Bottom card con preview del evento seleccionado ---
            selectedEvent?.let { event ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // --- Estado vacio: sin eventos con ubicacion ---
            if (!state.isLoading && eventsOnMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                ) {
                    EmptyState(
                        icon = Icons.Filled.Event,
                        title = "Sin eventos en el mapa",
                        message = if (state.searchQuery.isNotBlank())
                            "No hay resultados para tu busqueda."
                        else
                            "Los eventos con ubicacion apareceran aqui. Crea uno con el boton +."
                    )
                }
            }

            // --- Indicador de carga inicial ---
            if (state.isLoading) {
                LoadingBox(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

/** Fila horizontal scrollable con chips "Todos / Mis eventos / categorias". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    selected: HomeFilter,
    onSelect: (HomeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected is HomeFilter.All,
            onClick = { onSelect(HomeFilter.All) },
            label = { Text("Todos") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        FilterChip(
            selected = selected is HomeFilter.Mine,
            onClick = { onSelect(HomeFilter.Mine) },
            label = { Text("Mis eventos") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        HomeFilter.CATEGORIES.forEach { cat ->
            FilterChip(
                selected = (selected as? HomeFilter.Category)?.name == cat,
                onClick = { onSelect(HomeFilter.Category(cat)) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
