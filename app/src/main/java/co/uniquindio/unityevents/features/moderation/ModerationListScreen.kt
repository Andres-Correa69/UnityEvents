package co.uniquindio.unityevents.features.moderation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.core.component.EmptyState
import co.uniquindio.unityevents.core.component.EventCard
import co.uniquindio.unityevents.core.component.LoadingBox
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.EventStatus

/**
 * Lista de eventos filtrada por estado (PENDING, APPROVED o REJECTED). En PENDING, cada
 * card expone acciones de aprobar / rechazar (con dialogo para capturar la razon).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationListScreen(
    onBack: () -> Unit,
    onEventClick: (eventId: String) -> Unit,
    viewModel: ModerationListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var rejectingEvent by remember { mutableStateOf<Event?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (state.filter) {
                        EventStatus.PENDING -> "Eventos pendientes"
                        EventStatus.APPROVED -> "Eventos aprobados"
                        EventStatus.REJECTED -> "Eventos rechazados"
                    })
                },
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
        when {
            state.isLoading -> LoadingBox(Modifier.padding(innerPadding))
            state.events.isEmpty() -> EmptyState(
                icon = Icons.Filled.EventBusy,
                title = "Sin eventos",
                message = "No hay eventos en este estado.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp,
                    start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.events, key = { it.id }) { event ->
                    Column {
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.filter == EventStatus.PENDING) {
                            Spacer(Modifier.size(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.approve(event.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Aprobar")
                                }
                                OutlinedButton(
                                    onClick = { rejectingEvent = event },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Cancel, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.size(4.dp))
                                    Text("Rechazar")
                                }
                            }
                        } else if (state.filter == EventStatus.REJECTED && !event.rejectionReason.isNullOrBlank()) {
                            Spacer(Modifier.size(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Text(
                                    text = "Motivo: ${event.rejectionReason}",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogo de rechazo: pide razon.
    rejectingEvent?.let { event ->
        var reason by remember(event.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectingEvent = null },
            title = { Text("Rechazar evento", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Describe por que rechazas \"${event.title}\". El creador vera esta razon.")
                    Spacer(Modifier.size(12.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Motivo del rechazo") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reject(event.id, reason.ifBlank { "Contenido no cumple los lineamientos." })
                        rejectingEvent = null
                    }
                ) { Text("Rechazar") }
            },
            dismissButton = {
                TextButton(onClick = { rejectingEvent = null }) { Text("Cancelar") }
            }
        )
    }
}
