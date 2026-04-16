package co.uniquindio.unityevents.features.register

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.core.utils.GoogleSignInHelper
import co.uniquindio.unityevents.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado UI de la pantalla de registro. Incluye los cuatro campos del formulario y los
 * flags comunes de carga / error / exito.
 */
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Se activa al completarse el registro — la Screen salta al grafo principal. */
    val registerSuccess: Boolean = false
)

/**
 * ViewModel del flujo de registro. Valida localmente los campos y delega a [AuthRepository]
 * la creacion de la cuenta. Ademas expone el flujo de Google Sign-In por conveniencia.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value, errorMessage = null) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, errorMessage = null) }

    /**
     * Valida los campos y, si todo es correcto, delega al repositorio la creacion de la cuenta.
     */
    fun onRegisterClick() {
        val s = _state.value
        val name = s.name.trim()
        val email = s.email.trim()

        val validationError = validate(name, email, s.password, s.confirmPassword)
        if (validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.registerWithEmail(name, email, s.password).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, registerSuccess = true) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) }
                }
            )
        }
    }

    /** Igual que en Login: pide el idToken a Credential Manager y lo cambia por sesion Firebase. */
    fun onGoogleSignInClick(activityContext: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            googleSignInHelper.requestGoogleIdToken(activityContext).fold(
                onSuccess = { idToken ->
                    authRepository.signInWithGoogle(idToken).fold(
                        onSuccess = {
                            _state.update { it.copy(isLoading = false, registerSuccess = true) }
                        },
                        onFailure = { e ->
                            _state.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) }
                        }
                    )
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No se pudo iniciar con Google: ${e.message ?: "error desconocido"}"
                        )
                    }
                }
            )
        }
    }

    fun onRegisterSuccessConsumed() {
        _state.update { it.copy(registerSuccess = false) }
    }

    // -------------------------------------------------------------------------
    // Helpers privados de validacion y mapeo de errores.
    // -------------------------------------------------------------------------

    private fun validate(name: String, email: String, password: String, confirm: String): String? {
        if (name.isBlank()) return "El nombre es obligatorio."
        if (email.isBlank()) return "El email es obligatorio."
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Email con formato invalido."
        if (password.length < 6) return "La contrasena debe tener al menos 6 caracteres."
        if (password != confirm) return "Las contrasenas no coinciden."
        return null
    }

    private fun mapAuthError(throwable: Throwable): String = when {
        throwable.message?.contains("already in use", ignoreCase = true) == true ->
            "Ya existe una cuenta con ese email."
        throwable.message?.contains("weak password", ignoreCase = true) == true ->
            "La contrasena es demasiado debil."
        throwable.message?.contains("badly formatted", ignoreCase = true) == true ->
            "Email con formato invalido."
        throwable.message?.contains("network", ignoreCase = true) == true ->
            "Problema de conexion. Intenta de nuevo."
        else -> throwable.message ?: "No se pudo crear la cuenta."
    }
}
