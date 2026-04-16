package co.uniquindio.unityevents.core.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import co.uniquindio.unityevents.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Envuelve el flujo moderno de Google Sign-In en Android: **Credential Manager API**
 * + **Google ID token request**. Reemplaza al deprecado `GoogleSignInClient`.
 *
 * Uso tipico desde un ViewModel / Screen:
 * ```
 * val idToken = googleSignInHelper.requestGoogleIdToken(activityContext).getOrThrow()
 * authRepository.signInWithGoogle(idToken)
 * ```
 *
 * Requiere:
 * - `BuildConfig.WEB_CLIENT_ID` definido (se lee de `local.properties`).
 * - Play Services actualizados en el dispositivo.
 * - SHA-1 del keystore registrado en Firebase Console.
 *
 * El parametro `context` pasado a [requestGoogleIdToken] debe ser el de una Activity, no el
 * del `Application`, ya que Credential Manager muestra la UI del sistema sobre la actividad.
 */
@Singleton
class GoogleSignInHelper @Inject constructor(
    private val credentialManager: CredentialManager
) {

    /**
     * Solicita al sistema un ID token de Google.
     *
     * @param activityContext Contexto de la Activity donde se mostrara el selector de cuentas.
     * @return [Result.success] con el JWT `idToken` o [Result.failure] con la causa
     *         (usuario cancelo, sin cuentas, sin Play Services, etc.).
     */
    suspend fun requestGoogleIdToken(activityContext: Context): Result<String> = runCatching {
        // Valida en tiempo de ejecucion que el Web Client ID este configurado.
        check(BuildConfig.WEB_CLIENT_ID.isNotBlank()) {
            "WEB_CLIENT_ID vacio. Agregalo a local.properties (ver docs/FIREBASE_SETUP.md)."
        }

        // Opciones de la solicitud: queremos un ID token firmado para este proyecto Firebase.
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            // `false` permite que el usuario elija cualquier cuenta Google, no solo las ya autorizadas.
            .setFilterByAuthorizedAccounts(false)
            // `false` evita que seleccione automaticamente la unica cuenta disponible.
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // Muestra el UI del sistema y suspende hasta que el usuario elija una cuenta o cancele.
        val response = credentialManager.getCredential(
            context = activityContext,
            request = request
        )

        // Extrae el ID token del CustomCredential devuelto por Google.
        val credential = response.credential
        require(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Credencial devuelta no es un GoogleIdTokenCredential: ${credential::class.simpleName}" }

        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}
