package co.uniquindio.unityevents.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Estado de la pantalla de Perfil. */
data class ProfileUiState(
    val user: User? = null,
    val myEvents: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /** Progreso hacia el siguiente nivel (0f..1f). Cada nivel requiere 500 puntos. */
    val levelProgress: Float get() = ((user?.points ?: 0) % 500) / 500f
}

/**
 * ViewModel de Perfil. Expone los datos del usuario y sus eventos creados.
 *
 * Importante: los errores de la query "mis eventos" (por ejemplo falta de indice compuesto
 * en Firestore la primera vez) NO deben borrar los datos del usuario. Por eso aplicamos
 * un `.catch { emptyList() }` localizado a `observeMyEvents`, y solo el error de
 * `observeCurrentUser` llega al catch externo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    eventsRepository: EventsRepository
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = profileRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(ProfileUiState(isLoading = false, user = null))
            } else {
                eventsRepository.observeMyEvents(user.uid)
                    // Si falla la query (indice faltante, permisos...) seguimos mostrando el perfil
                    // con lista vacia en vez de tumbar el state completo a user=null.
                    .catch { emit(emptyList()) }
                    .onStart { emit(emptyList()) } // renderizamos el perfil antes de que lleguen eventos
                    .map { events -> ProfileUiState(user = user, myEvents = events, isLoading = false) }
            }
        }
        .catch { e ->
            emit(ProfileUiState(isLoading = false, errorMessage = e.message))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState(isLoading = true))
}
