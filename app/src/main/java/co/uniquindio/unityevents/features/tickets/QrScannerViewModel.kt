package co.uniquindio.unityevents.features.tickets

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
 * ViewModel del escaner de QR. Cuando la Screen detecta un codigo, llama a [onScanned]
 * para validar el ticket contra Firestore y marcarlo como usado.
 */
@HiltViewModel
class QrScannerViewModel @Inject constructor(
    private val ticketsRepository: TicketsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QrScannerUiState())
    val state: StateFlow<QrScannerUiState> = _state.asStateFlow()

    /** Evita procesar el mismo QR multiples veces seguidas. */
    private var lastProcessedPayload: String? = null

    /**
     * Invocado desde la Screen al detectar un payload valido. Si el payload es el
     * mismo que el ultimo procesado, se ignora (evita loops).
     */
    fun onScanned(payload: String) {
        if (payload.isBlank() || payload == lastProcessedPayload) return
        if (_state.value.isValidating) return
        lastProcessedPayload = payload

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isValidating = true,
                    errorMessage = null,
                    lastScannedTicket = null,
                    wasAlreadyUsed = false
                )
            }
            ticketsRepository.markTicketUsed(payload).fold(
                onSuccess = { ticket ->
                    val wasPreviouslyUsed = ticket.isUsed && lastProcessedPayload == payload &&
                        _state.value.lastScannedTicket == null &&
                        // Si la fecha de uso es muy reciente significa que lo acabamos de marcar ahora.
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
                        it.copy(
                            isValidating = false,
                            errorMessage = e.message ?: "QR invalido."
                        )
                    }
                }
            )
        }
    }

    /** Permite volver a escanear el mismo payload (tras mostrar el resultado). */
    fun onDismissResult() {
        _state.update { QrScannerUiState() }
        lastProcessedPayload = null
    }
}
