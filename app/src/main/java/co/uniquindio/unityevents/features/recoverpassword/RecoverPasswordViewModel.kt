package co.uniquindio.unityevents.features.recoverpassword

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
 * Estado de la interfaz de usuario para la pantalla de Recuperacion de Contrasena.
 *
 * @param code Codigo de recuperacion ingresado por el usuario.
 * @param newPassword Nueva contrasena ingresada por el usuario.
 * @param confirmPassword Confirmacion de la nueva contrasena.
 * @param isPasswordVisible Indica si la nueva contrasena se muestra en texto plano.
 * @param isConfirmPasswordVisible Indica si la confirmacion se muestra en texto plano.
 * @param isLoading Indica si se esta procesando el restablecimiento.
 */
data class RecoverPasswordUiState(
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)

/**
 * ViewModel para la pantalla de Recuperacion de Contrasena.
 *
 * Gestiona el estado del formulario de restablecimiento de contrasena,
 * valida el codigo de recuperacion y las nuevas contrasenas, y ejecuta
 * el restablecimiento a traves del repositorio.
 *
 * @param email Correo electronico del usuario que solicita el restablecimiento.
 * @param userRepository Repositorio de usuarios para validar y restablecer la contrasena.
 */
class RecoverPasswordViewModel(
    private val email: String,
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado mutable interno de la pantalla */
    private val _uiState = MutableStateFlow(RecoverPasswordUiState())

    /** Estado inmutable expuesto a la UI */
    val uiState: StateFlow<RecoverPasswordUiState> = _uiState.asStateFlow()

    /** Flujo de eventos de Snackbar (eventos de una sola vez) */
    private val _snackbarEvent = MutableSharedFlow<String>()

    /** Flujo inmutable de eventos de Snackbar expuesto a la UI */
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    /** Flujo de eventos de navegacion para indicar restablecimiento exitoso */
    private val _navigationEvent = MutableSharedFlow<Boolean>()

    /** Flujo inmutable de eventos de navegacion expuesto a la UI */
    val navigationEvent: SharedFlow<Boolean> = _navigationEvent.asSharedFlow()

    /**
     * Actualiza el codigo de recuperacion en el estado de la UI.
     *
     * @param code Nuevo valor del codigo de recuperacion.
     */
    fun onCodeChanged(code: String) {
        _uiState.update { it.copy(code = code) }
    }

    /**
     * Actualiza la nueva contrasena en el estado de la UI.
     *
     * @param password Nuevo valor de la contrasena.
     */
    fun onNewPasswordChanged(password: String) {
        _uiState.update { it.copy(newPassword = password) }
    }

    /**
     * Actualiza la confirmacion de la nueva contrasena en el estado de la UI.
     *
     * @param confirmPassword Nuevo valor de la confirmacion de contrasena.
     */
    fun onConfirmPasswordChanged(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    /**
     * Alterna la visibilidad de la nueva contrasena.
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
     * Ejecuta el proceso de restablecimiento de contrasena.
     *
     * Valida el codigo de recuperacion, la nueva contrasena y su confirmacion.
     * Si todo es correcto, restablece la contrasena mediante el repositorio.
     * Emite eventos de Snackbar para retroalimentar al usuario.
     */
    fun resetPassword() {
        val state = _uiState.value

        // Validar que el codigo no este vacio
        if (state.code.isBlank()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Por favor ingresa el codigo de recuperacion.")
            }
            return
        }

        // Validar que el codigo tenga 6 digitos
        if (state.code.length != 6) {
            viewModelScope.launch {
                _snackbarEvent.emit("El codigo de recuperacion debe tener 6 digitos.")
            }
            return
        }

        // Validar que la nueva contrasena tenga al menos 6 caracteres
        if (state.newPassword.length < 6) {
            viewModelScope.launch {
                _snackbarEvent.emit("La contrasena debe tener al menos 6 caracteres.")
            }
            return
        }

        // Validar que las contrasenas coincidan
        if (state.newPassword != state.confirmPassword) {
            viewModelScope.launch {
                _snackbarEvent.emit("Las contrasenas no coinciden.")
            }
            return
        }

        // Activar estado de carga
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Intentar restablecer la contrasena con el repositorio
            val success = userRepository.resetPassword(
                email = email,
                code = state.code,
                newPassword = state.newPassword
            )

            // Desactivar estado de carga
            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                // Restablecimiento exitoso: notificar y navegar al login
                _snackbarEvent.emit("Contrasena restablecida exitosamente. Inicia sesion con tu nueva contrasena.")
                _navigationEvent.emit(true)
            } else {
                // Codigo invalido: notificar al usuario
                _snackbarEvent.emit("El codigo de recuperacion no es valido.")
            }
        }
    }

    companion object {
        /**
         * Factory para crear instancias de [RecoverPasswordViewModel] con los parametros inyectados.
         *
         * @param email Correo electronico del usuario que solicita la recuperacion.
         * @param userRepository Repositorio de usuarios a inyectar.
         * @return [ViewModelProvider.Factory] configurada para crear [RecoverPasswordViewModel].
         */
        fun provideFactory(
            email: String,
            userRepository: UserRepository
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RecoverPasswordViewModel(email, userRepository)
                }
            }
    }
}
