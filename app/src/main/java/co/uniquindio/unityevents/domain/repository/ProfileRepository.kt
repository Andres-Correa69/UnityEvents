package co.uniquindio.unityevents.domain.repository

import android.net.Uri
import co.uniquindio.unityevents.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Gestion del perfil del usuario actual.
 * Internamente combina Firebase Auth (para displayName/photo) con Firestore (bio, city, role, level, points).
 */
interface ProfileRepository {

    /** Flujo del perfil del usuario actual (se actualiza en vivo). */
    fun observeCurrentUser(): Flow<User?>

    /** Lee el perfil de un usuario por uid (por ejemplo para ver el perfil del organizador). */
    suspend fun getUser(uid: String): Result<User?>

    /** Actualiza los campos editables del perfil (nombre, bio, ciudad). */
    suspend fun updateProfile(
        displayName: String,
        bio: String,
        city: String
    ): Result<Unit>

    /**
     * Sube una nueva foto de perfil a `/users/{uid}/avatar.jpg` y actualiza:
     * - `FirebaseAuth.currentUser.photoUrl`
     * - Campo `photoUrl` en `/users/{uid}`
     */
    suspend fun updatePhoto(imageUri: Uri): Result<String>

    /**
     * Agrega un FCM token al array `fcmTokens` del usuario actual (idempotente, usa
     * arrayUnion). Las Cloud Functions leen este array para enviar push notifications.
     * No-op si no hay usuario logueado.
     */
    suspend fun saveFcmToken(token: String): Result<Unit>

    /**
     * Quita un FCM token del array. Util cuando el token deja de ser valido (logout o
     * desinstalacion en otro dispositivo).
     */
    suspend fun removeFcmToken(token: String): Result<Unit>
}
