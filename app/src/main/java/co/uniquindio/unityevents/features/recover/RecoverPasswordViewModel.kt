package co.uniquindio.unityevents.features.recover

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla "Recuperar contrasena". Solo se pide email; el resto son flags.
 */
data class RecoverPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel que envia el correo de recuperacion mediante [AuthRepository.sendPasswordReset].
 * No realiza navegacion; solo muestra un mensaje de exito/error en la misma pantalla.
 */
@HiltViewModel
class RecoverPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecoverPasswordUiState())
    val state: StateFlow<RecoverPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, errorMessage = null, successMessage = null) }
    }

    fun onSendClick() {
        val email = _state.value.email.trim()

        // Validaciones locales.
        if (email.isBlank()) {
            _state.update { it.copy(errorMessage = "El email es obligatorio.") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(errorMessage = "Email con formato invalido.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            authRepository.sendPasswordReset(email).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Enviamos un correo a $email con instrucciones."
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "No se pudo enviar el correo."
                        )
                    }
                }
            )
        }
    }
}
