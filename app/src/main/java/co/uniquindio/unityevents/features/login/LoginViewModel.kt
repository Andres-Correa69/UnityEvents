package co.uniquindio.unityevents.features.login

import android.content.Context
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
 * Estado inmutable que renderiza [LoginScreen]. Cambia mediante `MutableStateFlow.update {}`
 * dentro del ViewModel; la UI lo observa con `collectAsStateWithLifecycle`.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Bandera one-shot: cuando vale `true`, la Screen dispara la navegacion a Home. */
    val loginSuccess: Boolean = false
)

/**
 * ViewModel de la pantalla de Login. Coordina validacion, llamadas al [AuthRepository] y
 * el flujo de Google Sign-In via [GoogleSignInHelper].
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInHelper: GoogleSignInHelper
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Actualiza el email cuando el usuario escribe. Limpia el error previo. */
    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, errorMessage = null) }
    }

    /** Actualiza la contrasena cuando el usuario escribe. Limpia el error previo. */
    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, errorMessage = null) }
    }

    /** Invocado al presionar "Iniciar sesion". Valida campos y llama al repositorio. */
    fun onLoginClick() {
        val snapshot = _state.value
        val email = snapshot.email.trim()
        val password = snapshot.password

        // Validaciones locales antes de ir a la red.
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(errorMessage = "Email y contrasena son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signInWithEmail(email, password)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, loginSuccess = true) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = mapAuthError(e)) }
                }
            )
        }
    }

    /**
     * Inicia el flujo de Google Sign-In: obtiene el ID token con Credential Manager y
     * lo intercambia por una credencial Firebase.
     *
     * @param activityContext contexto de la Activity donde se mostrara el selector de cuentas.
     */
    fun onGoogleSignInClick(activityContext: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val tokenResult = googleSignInHelper.requestGoogleIdToken(activityContext)
            tokenResult.fold(
                onSuccess = { idToken ->
                    authRepository.signInWithGoogle(idToken).fold(
                        onSuccess = {
                            _state.update { it.copy(isLoading = false, loginSuccess = true) }
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

    /** La Screen llama esto despues de consumir el evento `loginSuccess` para resetear la bandera. */
    fun onLoginSuccessConsumed() {
        _state.update { it.copy(loginSuccess = false) }
    }

    /** Traduce excepciones comunes de Firebase a mensajes legibles en espanol. */
    private fun mapAuthError(throwable: Throwable): String = when {
        throwable.message?.contains("password is invalid", ignoreCase = true) == true ->
            "Contrasena incorrecta."
        throwable.message?.contains("no user record", ignoreCase = true) == true ->
            "No existe una cuenta con ese email."
        throwable.message?.contains("badly formatted", ignoreCase = true) == true ->
            "Email con formato invalido."
        throwable.message?.contains("network", ignoreCase = true) == true ->
            "Problema de conexion. Intenta de nuevo."
        else -> throwable.message ?: "No se pudo iniciar sesion."
    }
}
