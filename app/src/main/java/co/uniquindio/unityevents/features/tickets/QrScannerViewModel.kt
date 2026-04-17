package co.uniquindio.unityevents.features.tickets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Ticket
import co.uniquindio.unityevents.domain.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la pantalla de escaner de QR. */
data class QrScannerUiState(
    val isValidating: Boolean = false,
    val lastScannedTicket: Ticket? = null,
    val wasAlreadyUsed: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel del escaner de QR del **organizador del evento**. Recibe como argumento el
 * `eventId` contra el cual validar. Si el QR escaneado pertenece a otro evento, se
 * rechaza con un mensaje claro.
 */
@HiltViewModel
class QrScannerViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val ticketsRepository: TicketsRepository
) : ViewModel() {

    /** Id del evento desde el que se esta escaneando (pasado via argumento de navegacion). */
    private val eventId: String = checkNotNull(savedState.get<String>("eventId")) {
        "QrScannerScreen requiere el argumento 'eventId'."
    }

    private val _state = MutableStateFlow(QrScannerUiState())
    val state: StateFlow<QrScannerUiState> = _state.asStateFlow()

    /** Evita procesar el mismo QR multiples veces seguidas (loop de deteccion). */
    private var lastProcessedPayload: String? = null

    fun onScanned(payload: String) {
        if (payload.isBlank() || payload == lastProcessedPayload) return
        if (_state.value.isValidating) return
        lastProcessedPayload = payload

        viewModelScope.launch {
            _state.update {
                it.copy(isValidating = true, errorMessage = null, lastScannedTicket = null, wasAlreadyUsed = false)
            }

            ticketsRepository.markTicketUsed(payload, expectedEventId = eventId).fold(
                onSuccess = { ticket ->
                    // Si `usedAt` ya existia antes de este escaneo, el repo lo devuelve sin cambios.
                    // Heuristica: si `usedAt` tiene > 5s de antiguedad, fue validado previamente.
                    val wasPreviouslyUsed = ticket.isUsed &&
                        (System.currentTimeMillis() - (ticket.usedAt?.time ?: 0L)) > 5_000L
                    _state.update {
                        it.copy(
                            isValidating = false,
                            lastScannedTicket = ticket,
                            wasAlreadyUsed = wasPreviouslyUsed
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isValidating = false, errorMessage = e.message ?: "QR invalido.")
                    }
                }
            )
        }
    }

    fun onDismissResult() {
        _state.update { QrScannerUiState() }
        lastProcessedPayload = null
    }
}
