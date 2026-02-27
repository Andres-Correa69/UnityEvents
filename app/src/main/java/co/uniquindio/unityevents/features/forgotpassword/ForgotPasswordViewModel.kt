package co.uniquindio.unityevents.features.forgotpassword

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
 * Estado de la interfaz de usuario para la pantalla de Olvido de Contrasena.
 *
 * @param email Correo electronico ingresado por el usuario.
 * @param isLoading Indica si se esta procesando el envio del codigo.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel para la pantalla de Olvido de Contrasena.
 *
 * Gestiona el estado del formulario de solicitud de recuperacion,
 * valida el email y genera un codigo de recuperacion a traves del repositorio.
 *
 * @param userRepository Repositorio de usuarios para verificar el email y generar codigos.
 */
class ForgotPasswordViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    /** Estado mutable interno de la pantalla */
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())

    /** Estado inmutable expuesto a la UI */
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    /** Flujo de eventos de Snackbar (eventos de una sola vez) */
    private val _snackbarEvent = MutableSharedFlow<String>()

    /** Flujo inmutable de eventos de Snackbar expuesto a la UI */
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    /** Flujo de eventos de navegacion con el email para la pantalla de recuperacion */
    private val _navigationEvent = MutableSharedFlow<String>()

    /** Flujo inmutable de eventos de navegacion expuesto a la UI */
    val navigationEvent: SharedFlow<String> = _navigationEvent.asSharedFlow()

    /**
     * Actualiza el email en el estado de la UI.
     *
     * @param email Nuevo valor del correo electronico.
     */
    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    /**
     * Ejecuta el proceso de envio del codigo de recuperacion.
     *
     * Valida el email ingresado y, si es correcto y existe en el sistema,
     * genera un codigo de recuperacion. Emite eventos de Snackbar para
     * retroalimentar al usuario y un evento de navegacion con el email
     * para pasar a la pantalla de recuperacion.
     */
    fun sendRecoveryCode() {
        val state = _uiState.value

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

        // Activar estado de carga
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // Intentar generar codigo de recuperacion
            val success = userRepository.generateRecoveryCode(state.email.trim())

            // Desactivar estado de carga
            _uiState.update { it.copy(isLoading = false) }

            if (success) {
                // Codigo generado: notificar y navegar a la pantalla de recuperacion
                _snackbarEvent.emit("Se ha enviado un codigo de recuperacion a tu correo. (Codigo: 123456)")
                _navigationEvent.emit(state.email.trim())
            } else {
                // Email no encontrado: notificar al usuario
                _snackbarEvent.emit("No se encontro una cuenta con ese correo electronico.")
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
         * Factory para crear instancias de [ForgotPasswordViewModel] con el repositorio inyectado.
         *
         * @param userRepository Repositorio de usuarios a inyectar.
         * @return [ViewModelProvider.Factory] configurada para crear [ForgotPasswordViewModel].
         */
        fun provideFactory(userRepository: UserRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ForgotPasswordViewModel(userRepository)
                }
            }
    }
}
