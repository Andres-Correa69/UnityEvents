package co.uniquindio.unityevents.data.repository

import android.net.Uri
import co.uniquindio.unityevents.data.model.UserDto
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Impl que combina Auth + Firestore para el perfil. Cuando Auth reporta cambio de sesion,
 * reactivamente se suscribe al documento `/users/{uid}` para emitir datos frescos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override fun observeCurrentUser(): Flow<User?> {
        // Flujo de uid (o null) alimentado por AuthStateListener.
        val uidFlow: Flow<String?> = callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
            firebaseAuth.addAuthStateListener(listener)
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
        // Por cada uid, nos suscribimos al doc correspondiente (o emitimos null si no hay sesion).
        return uidFlow.flatMapLatest { uid ->
            if (uid == null) flowOf(null) else observeUserDoc(uid)
        }
    }

    override suspend fun getUser(uid: String): Result<User?> = runCatching {
        val snap = firestore.collection("users").document(uid).get().await()
        snap.toObject(UserDto::class.java)?.toDomain()
    }

    override suspend fun updateProfile(
        displayName: String,
        bio: String,
        city: String
    ): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: error("Debes iniciar sesion para editar el perfil.")

        // 1) Actualiza displayName en Auth (afecta a otros consumidores).
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        ).await()

        // 2) Mergea los cambios en el doc Firestore.
        firestore.collection("users").document(user.uid).set(
            mapOf(
                "displayName" to displayName,
                "bio" to bio,
                "city" to city,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
    }

    override suspend fun updatePhoto(imageUri: Uri): Result<String> = runCatching {
        val user = firebaseAuth.currentUser
            ?: error("Debes iniciar sesion para cambiar la foto.")
        val ref = storage.reference.child("users/${user.uid}/avatar.jpg")
        ref.putFile(imageUri).await()
        val url = ref.downloadUrl.await().toString()

        // Actualiza en Auth.
        user.updateProfile(
            UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(url)).build()
        ).await()

        // Mergea en Firestore.
        firestore.collection("users").document(user.uid)
            .set(mapOf("photoUrl" to url), SetOptions.merge())
            .await()

        url
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private fun observeUserDoc(uid: String): Flow<User?> = callbackFlow {
        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val user = snap?.toObject(UserDto::class.java)
                    ?.copy(uid = snap.id)
                    ?.toDomain(emailVerified = firebaseAuth.currentUser?.isEmailVerified ?: false)
                trySend(user)
            }
        awaitClose { registration.remove() }
    }
}
