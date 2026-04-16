package co.uniquindio.unityevents.features.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Ticket
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import co.uniquindio.unityevents.domain.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado de la pantalla "Mis tickets". */
data class MyTicketsUiState(
    val tickets: List<Ticket> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** ViewModel que expone los tickets del usuario autenticado. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyTicketsViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    ticketsRepository: TicketsRepository
) : ViewModel() {

    val state: StateFlow<MyTicketsUiState> = profileRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            // `.catch` en el flow interno para que un error en la query (p.ej. indice faltante)
            // no tumbe el StateFlow entero.
            else ticketsRepository.observeMyTickets(user.uid).catch { emit(emptyList()) }
        }
        .map { tickets -> MyTicketsUiState(tickets = tickets, isLoading = false) }
        .catch { e ->
            emit(MyTicketsUiState(isLoading = false, errorMessage = e.message ?: "Error al cargar tickets"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyTicketsUiState(isLoading = true))
}
