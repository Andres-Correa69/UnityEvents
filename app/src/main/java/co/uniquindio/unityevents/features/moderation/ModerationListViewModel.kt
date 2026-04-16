package co.uniquindio.unityevents.features.moderation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.EventStatus
import co.uniquindio.unityevents.domain.repository.EventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la lista de eventos filtrada por estado. */
data class ModerationListUiState(
    val filter: EventStatus = EventStatus.PENDING,
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel generico: segun el argumento de navegacion `filter`, muestra eventos PENDING,
 * APPROVED o REJECTED, y permite al moderador cambiar el estado.
 */
@HiltViewModel
class ModerationListViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val filter: EventStatus = EventStatus.fromString(savedState.get<String>("filter"))

    val state: StateFlow<ModerationListUiState> = eventsRepository.observeEventsByStatus(filter)
        .map { list -> ModerationListUiState(filter = filter, events = list, isLoading = false) }
        .catch { e -> emit(ModerationListUiState(filter = filter, isLoading = false, errorMessage = e.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModerationListUiState(filter = filter, isLoading = true))

    fun approve(eventId: String) = viewModelScope.launch {
        eventsRepository.updateStatus(eventId, EventStatus.APPROVED)
    }

    fun reject(eventId: String, reason: String) = viewModelScope.launch {
        eventsRepository.updateStatus(eventId, EventStatus.REJECTED, reason)
    }
}
