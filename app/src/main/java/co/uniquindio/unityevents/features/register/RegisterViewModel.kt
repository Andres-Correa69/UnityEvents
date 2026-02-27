package co.uniquindio.unityevents.features.register

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
 * Estado de la interfaz de usuario para la pantalla de Registro.
 *
 * @param name Nombre completo ingresado por el usuario.
 * @param email Correo electronico ingresado por el usuario.
 * @param password Contrasena ingresada por el usuario.
 * @param confirmPassword Confirmacion de la contrasena.
 * @param isPasswordVisible Indica si la contrasena se muestra en texto plano.
 * @param isConfirmPasswordVisible Indica si la confirmacion de contrasena se muestra en texto plano.
 * @param isLoading Indica si se esta procesando el registro.
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel para la pantalla de Registro.
 *
 * Gestiona el estado del formulario de registro, realiza validaciones
 * de todos los campos y ejecuta el registro de nuevos usuarios
 * contra el repositorio.
 *
 * @param userRepository Repositorio de usuarios para el registro.
 */
class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado mutable interno de la pantalla */
    private val _uiState = MutableStateFlow(RegisterUiState())

    /** Estado inmutable expuesto a la UI */
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /** Flujo de eventos de Snackbar (eventos de una sola vez) */
    private val _snackbarEvent = MutableSharedFlow<String>()

    /** Flujo inmutable de eventos de Snackbar expuesto a la UI */
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    /** Flujo de eventos de navegacion para indicar registro exitoso */
    private val _navigationEvent = MutableSharedFlow<Boolean>()

    /** Flujo inmutable de eventos de navegacion expuesto a la UI */
    val navigationEvent: SharedFlow<Boolean> = _navigationEvent.asSharedFlow()

    /**
     * Actualiza el nombre en el estado de la UI.
     *
     * @param name Nuevo valor del nombre.
     */
    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name) }
    }

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
     * Actualiza la confirmacion de contrasena en el estado de la UI.
     *
     * @param confirmPassword Nuevo valor de la confirmacion de contrasena.
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    /**
     * Alterna la visibilidad de la contrasena.
     */
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    /**
     * Alterna la visibilidad de la confirmacion de contrasena.
     */
    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    /**
     * Ejecuta el proceso de registro de un nuevo usuario.
     *
     * Valida todos los campos del formulario y, si son correctos,
     * registra al usuario mediante el repositorio. Emite eventos
     * de Snackbar para retroalimentar al usuario sobre el resultado.
     */
    fun register() {
        val state = _uiState.value

        // Validar que el nombre no este vacio y tenga al menos 3 caracteres
        if (state.name.isBlank() || state.name.trim().length < 3) {
            viewModelScope.launch {
                _snackbarEvent.emit("El nombre debe tener al menos 3 caracteres.")
            }
            return
        }

        // Validar que el email no este vacio
        if (state.email.isBlank()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Por favor ingresa tu correo electronico.")
            }
            return
        }

        // Validar formato de email
        if (!isValidEmail(state.email)) {
            viewModelScope.launch {
                _snackbarEvent.emit("El formato del correo electronico no es valido.")
            }
            return
        }

        // Validar que la contrasena tenga al menos 6 caracteres
        if (state.password.length < 6) {
            viewModelScope.launch {
                _snackbarEvent.emit("La contrasena debe tener al menos 6 caracteres.")
            }
            return
        }

        // Validar que las contrasenas coincidan
        if (state.password != state.confirmPassword) {
            viewModelScope.launch {
                _snackbarEvent.emit("Las contrasenas no coinciden.")
            }
            return
        }

        // Activar estado de carga
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Intentar registro con el repositorio
            val success = userRepository.register(
                name = state.name.trim(),
                email = state.email.trim(),
                password = state.password
            )

            // Desactivar estado de carga
            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                // Registro exitoso: notificar y navegar
                _snackbarEvent.emit("Registro exitoso. Ahora puedes iniciar sesion.")
                _navigationEvent.emit(true)
            } else {
                // Email ya registrado: notificar al usuario
                _snackbarEvent.emit("Este correo electronico ya esta registrado.")
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
         * Factory para crear instancias de [RegisterViewModel] con el repositorio inyectado.
         *
         * @param userRepository Repositorio de usuarios a inyectar.
         * @return [ViewModelProvider.Factory] configurada para crear [RegisterViewModel].
         */
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RegisterViewModel(userRepository)
                }
            }
    }
}
