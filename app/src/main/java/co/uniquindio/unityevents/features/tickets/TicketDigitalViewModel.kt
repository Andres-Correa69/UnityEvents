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

/** Estado del detalle de ticket (muestra el QR). */
data class TicketDigitalUiState(
    val ticket: Ticket? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** Carga el ticket por id y lo expone para renderizar el QR. */
@HiltViewModel
class TicketDigitalViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val ticketsRepository: TicketsRepository
) : ViewModel() {

    private val ticketId: String = checkNotNull(savedState.get<String>("ticketId")) {
        "TicketDigitalScreen requiere argumento 'ticketId'."
    }

    private val _state = MutableStateFlow(TicketDigitalUiState(isLoading = true))
    val state: StateFlow<TicketDigitalUiState> = _state.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        ticketsRepository.getTicket(ticketId).fold(
            onSuccess = { ticket ->
                _state.update { it.copy(isLoading = false, ticket = ticket) }
            },
            onFailure = { e ->
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "No se pudo cargar el ticket.")
                }
            }
        )
    }
}
