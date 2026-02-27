package co.uniquindio.unityevents.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado de la interfaz de usuario para la pantalla Home.
 *
 * @param currentUser Usuario actualmente autenticado, null si no hay sesion.
 */
data class HomeUiState(
    val currentUser: User? = null
)

/**
 * ViewModel para la pantalla Home.
 *
 * Gestiona el estado de la pantalla principal, incluyendo la informacion
 * del usuario autenticado y la funcionalidad de cierre de sesion.
 *
 * @param userRepository Repositorio de usuarios para consultar la sesion actual.
 */
class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado mutable interno de la pantalla */
    private val _uiState = MutableStateFlow(HomeUiState())

    /** Estado inmutable expuesto a la UI */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Cargar los datos del usuario actual al inicializar el ViewModel
        loadCurrentUser()
    }

    /**
     * Carga la informacion del usuario actualmente autenticado
     * desde el repositorio y actualiza el estado de la UI.
     */
    private fun loadCurrentUser() {
        val user = userRepository.getCurrentUser()
        _uiState.value = HomeUiState(currentUser = user)
    }

    /**
     * Cierra la sesion del usuario actual en el repositorio.
     */
    fun logout() {
        userRepository.logout()
    }

    companion object {
        /**
         * Factory para crear instancias de [HomeViewModel] con el repositorio inyectado.
         *
         * @param userRepository Repositorio de usuarios a inyectar.
         * @return [ViewModelProvider.Factory] configurada para crear [HomeViewModel].
         */
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    HomeViewModel(userRepository)
                }
            }
    }
}
