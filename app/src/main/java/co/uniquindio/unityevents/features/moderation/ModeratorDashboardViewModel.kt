package co.uniquindio.unityevents.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.EventStatus
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado agregado del panel de moderacion (conteos por estado). */
data class ModeratorDashboardUiState(
    val pendingEvents: Int = 0,
    val approvedEvents: Int = 0,
    val rejectedEvents: Int = 0,
    val pendingReports: Int = 0,
    val isLoading: Boolean = true
)

/**
 * ViewModel del panel de moderacion. Combina contadores de eventos por estado + reportes
 * pendientes. Los datos detallados se ven en ModerationListScreen.
 */
@HiltViewModel
class ModeratorDashboardViewModel @Inject constructor(
    eventsRepository: EventsRepository,
    reportsRepository: ReportsRepository
) : ViewModel() {

    val state: StateFlow<ModeratorDashboardUiState> = combine(
        eventsRepository.observeEventsByStatus(EventStatus.PENDING),
        eventsRepository.observeEventsByStatus(EventStatus.APPROVED),
        eventsRepository.observeEventsByStatus(EventStatus.REJECTED),
        reportsRepository.observePendingReports()
    ) { pending, approved, rejected, reports ->
        ModeratorDashboardUiState(
            pendingEvents = pending.size,
            approvedEvents = approved.size,
            rejectedEvents = rejected.size,
            pendingReports = reports.size,
            isLoading = false
        )
    }
    .catch { emit(ModeratorDashboardUiState(isLoading = false)) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModeratorDashboardUiState(isLoading = true))
}
