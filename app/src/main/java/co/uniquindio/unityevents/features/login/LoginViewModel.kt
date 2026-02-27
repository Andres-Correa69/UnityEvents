package co.uniquindio.unityevents.features.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.uniquindio.unityevents.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de la interfaz de usuario para la pantalla de Login.
 *
 * @param email Correo electronico ingresado por el usuario.
 * @param password Contrasena ingresada por el usuario.
 * @param isPasswordVisible Indica si la contrasena se muestra en texto plano.
 * @param isLoading Indica si se esta procesando la autenticacion.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel para la pantalla de Login.
 *
 * Gestiona el estado del formulario de inicio de sesion, realiza
 * validaciones de los campos y ejecuta la autenticacion contra
 * el repositorio de usuarios.
 *
 * @param userRepository Repositorio de usuarios para autenticacion.
 */
class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado mutable interno de la pantalla */
    private val _uiState = MutableStateFlow(LoginUiState())

    /** Estado inmutable expuesto a la UI */
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /** Flujo de eventos de Snackbar (eventos de una sola vez) */
    private val _snackbarEvent = MutableSharedFlow<String>()

    /** Flujo inmutable de eventos de Snackbar expuesto a la UI */
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    /** Flujo de eventos de navegacion para indicar login exitoso */
    private val _navigationEvent = MutableSharedFlow<Boolean>()

    /** Flujo inmutable de eventos de navegacion expuesto a la UI */
    val navigationEvent: SharedFlow<Boolean> = _navigationEvent.asSharedFlow()

    /**
     * Actualiza el email en el estado de la UI.
     *
     * @param email Nuevo valor del correo electronico.
     */
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Actualiza la contrasena en el estado de la UI.
     *
     * @param password Nuevo valor de la contrasena.
     */
    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    /**
     * Alterna la visibilidad de la contrasena entre texto plano y oculto.
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Ejecuta el proceso de inicio de sesion.
     *
     * Valida los campos del formulario y, si son correctos, intenta
     * autenticar al usuario mediante el repositorio. Emite eventos
     * de Snackbar para retroalimentar al usuario sobre el resultado.
     */
    fun login() {
        val state = _uiState.value

        // Validar que el email no este vacio
        if (state.email.isBlank()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Por favor ingresa tu correo electronico.")
            }
            return
        }

        // Validar formato de email con expresion regular
        if (!isValidEmail(state.email)) {
            viewModelScope.launch {
                _snackbarEvent.emit("El formato del correo electronico no es valido.")
            }
            return
        }

        // Validar que la contrasena no este vacia
        if (state.password.isBlank()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Por favor ingresa tu contrasena.")
            }
            return
        }

        // Activar estado de carga
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Intentar autenticacion con el repositorio
            val user = userRepository.authenticate(state.email.trim(), state.password)

            // Desactivar estado de carga
            _uiState.update { it.copy(isLoading = false) }

            if (user != null) {
                // Login exitoso: emitir evento de navegacion
                _navigationEvent.emit(true)
            } else {
                // Login fallido: notificar al usuario
                _snackbarEvent.emit("Credenciales incorrectas. Verifica tu email y contrasena.")
            }
        }
    }

    /**
     * Valida el formato de un correo electronico usando una expresion regular.
     *
     * @param email Correo electronico a validar.
     * @return true si el formato es valido, false en caso contrario.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailRegex.matches(email.trim())
    }

    companion object {
        /**
         * Factory para crear instancias de [LoginViewModel] con el repositorio inyectado.
         *
         * @param userRepository Repositorio de usuarios a inyectar.
         * @return [ViewModelProvider.Factory] configurada para crear [LoginViewModel].
         */
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    LoginViewModel(userRepository)
                }
            }
    }
}
