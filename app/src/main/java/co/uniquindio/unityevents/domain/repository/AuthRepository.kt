package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de autenticacion. Lo consume la capa `features/` (ViewModels) y lo implementa
 * `data/repository/AuthRepositoryImpl` contra Firebase Auth + Firestore.
 *
 * Todas las operaciones de escritura devuelven `Result<T>` para que el ViewModel decida
 * como mostrar el error (Snackbar, supportingText, etc.). `observeAuthState` es un `Flow`
 * frio que emite el usuario actual cada vez que Firebase reporta un cambio de sesion.
 */
interface AuthRepository {

    /**
     * Inicia sesion con email y contrasena.
     * @return `Result.success(User)` si las credenciales son validas, o `Result.failure` con la causa.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<User>

    /**
     * Crea una cuenta nueva con email/contrasena, actualiza el displayName en Firebase Auth
     * y escribe un documento en `/users/{uid}` en Firestore con el perfil inicial.
     */
    suspend fun registerWithEmail(name: String, email: String, password: String): Result<User>

    /**
     * Intercambia un ID token de Google (obtenido con Credential Manager) por una credencial
     * de Firebase Auth. Si es el primer login, crea el documento `/users/{uid}` en Firestore.
     *
     * @param idToken JWT emitido por Google para este Web Client ID.
     */
    suspend fun signInWithGoogle(idToken: String): Result<User>

    /**
     * Envia un correo de recuperacion de contrasena. Firebase decide el idioma segun `auth.setLanguageCode`.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /** Cierra la sesion local (no requiere red). */
    fun signOut()

    /**
     * Flujo reactivo con el usuario actualmente autenticado. Emite `null` cuando no hay sesion.
     * Se alimenta de `FirebaseAuth.AuthStateListener`.
     */
    fun observeAuthState(): Flow<User?>
}
