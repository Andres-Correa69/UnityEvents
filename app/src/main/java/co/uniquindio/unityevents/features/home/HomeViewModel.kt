package co.uniquindio.unityevents.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Filtros disponibles en Home.
 *
 * - [All]       → feed combinado: aprobados publicos + mis eventos (cualquier estado).
 * - [Mine]      → solo mis eventos, en cualquier estado.
 * - [Category]  → eventos (aprobados publicos + mios) de una categoria especifica.
 */
sealed interface HomeFilter {
    data object All : HomeFilter
    data object Mine : HomeFilter
    data class Category(val name: String) : HomeFilter

    companion object {
        /** Categorias que aparecen como chips en la UI (mismas del formulario de crear evento). */
        val CATEGORIES = listOf("Academico", "Cultural", "Deportivo", "Social", "Tecnologia", "Otro")
    }
}

/** Estado de la pantalla principal (feed con buscador y filtros). */
data class HomeUiState(
    /** Aprobados del feed publico (cualquier organizador). */
    val approvedEvents: List<Event> = emptyList(),
    /** Eventos del usuario actual, en cualquier estado (incluye pendientes y rechazados). */
    val myEvents: List<Event> = emptyList(),
    val currentUser: User? = null,
    val searchQuery: String = "",
    val filter: HomeFilter = HomeFilter.All,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    /**
     * Lista final visible: aplica filtro + busqueda.
     *
     * - ALL       → aprobados publicos + mis eventos (cualquier estado), deduplicados.
     * - MINE      → solo mis eventos, cualquier estado.
     * - CATEGORY  → aprobados publicos + mis eventos, filtrados por categoria.
     */
    val visibleEvents: List<Event> get() {
        val base: List<Event> = when (val f = filter) {
            HomeFilter.All -> (myEvents + approvedEvents).distinctBy { it.id }
            HomeFilter.Mine -> myEvents
            is HomeFilter.Category ->
                (myEvents + approvedEvents)
                    .distinctBy { it.id }
                    .filter { it.category.equals(f.name, ignoreCase = true) }
        }
        return if (searchQuery.isBlank()) base
        else base.filter { e ->
            val q = searchQuery.trim()
            e.title.contains(q, ignoreCase = true) ||
                e.description.contains(q, ignoreCase = true) ||
                e.placeName.contains(q, ignoreCase = true)
        }
    }
}

/**
 * ViewModel de Home. Expone perfil + eventos aprobados + mis eventos + estado de busqueda
 * y filtros como un unico [HomeUiState]. Los `.catch` localizados en cada flow garantizan
 * que un error transitorio (por ejemplo un indice compuesto que aun no existe) no tumbe
 * el perfil ni el feed principal.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    eventsRepository: EventsRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow<HomeFilter>(HomeFilter.All)

    val state: StateFlow<HomeUiState> = profileRepository.observeCurrentUser()
        .catch { emit(null) }
        .flatMapLatest { user ->
            combine(
                eventsRepository.observeApprovedEvents().catch { emit(emptyList()) },
                if (user != null) eventsRepository.observeMyEvents(user.uid).catch { emit(emptyList()) }
                else flowOf(emptyList()),
                _searchQuery,
                _filter
            ) { approved, mine, query, filter ->
                HomeUiState(
                    approvedEvents = approved,
                    myEvents = mine,
                    currentUser = user,
                    searchQuery = query,
                    filter = filter,
                    isLoading = false
                )
            }
        }
        .catch { e ->
            emit(HomeUiState(isLoading = false, errorMessage = e.message ?: "Error al cargar eventos"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    /** Actualiza la cadena del buscador (la UI llama esto en cada cambio del textfield). */
    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    /** Cambia el filtro activo (chip seleccionado). */
    fun onFilterChange(filter: HomeFilter) {
        _filter.value = filter
    }
}
