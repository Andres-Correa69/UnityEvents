package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.UserDto
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion por defecto de [AuthRepository] usando Firebase Auth + Firestore.
 *
 * - Auth: gestiona las credenciales (email/password y Google).
 * - Firestore: guarda el perfil publico del usuario en `/users/{uid}`.
 *
 * Todas las operaciones asincronas usan `kotlinx-coroutines-play-services`.`await()` para
 * integrar las `Task` de Firebase con coroutines.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Result<User> =
        runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: error("Firebase no devolvio usuario tras signInWithEmailAndPassword")
            firebaseUser.toDomain()
        }

    override suspend fun registerWithEmail(
        name: String,
        email: String,
        password: String
    ): Result<User> = runCatching {
        // 1) Crea usuario en Firebase Auth.
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user
            ?: error("Firebase no devolvio usuario tras createUserWithEmailAndPassword")

        // 2) Actualiza el displayName para que aparezca en UI y proximos logins.
        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name).build()
        ).await()

        // 3) Crea el documento /users/{uid} con el perfil inicial.
        val user = User(
            uid = firebaseUser.uid,
            displayName = name,
            email = email,
            photoUrl = firebaseUser.photoUrl?.toString(),
            emailVerified = firebaseUser.isEmailVerified
        )
        upsertUserDocument(user)

        user
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        // Intercambia el ID token de Google por una credencial Firebase.
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = result.user
            ?: error("Firebase no devolvio usuario tras signInWithCredential")

        val user = firebaseUser.toDomain()

        // Si es el primer login (isNewUser == true), crea el documento en Firestore.
        // En logins subsiguientes solo actualiza updatedAt mediante SetOptions.merge().
        upsertUserDocument(user)

        user
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Observa los cambios de sesion registrando un `AuthStateListener` y propagando
     * el [User] actual (o null) al flujo. Se desregistra al cancelar la coleccion.
     */
    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    /**
     * Crea o actualiza el documento del usuario en Firestore. Usa `SetOptions.merge()`
     * para no sobrescribir `createdAt` en login recurrente.
     */
    private suspend fun upsertUserDocument(user: User) {
        val dto = UserDto.fromDomain(user)
        firestore.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(dto, SetOptions.merge())
            .await()
    }

    /** Mapea un [FirebaseUser] al modelo de dominio [User]. */
    private fun FirebaseUser.toDomain(): User = User(
        uid = uid,
        displayName = displayName.orEmpty(),
        email = email.orEmpty(),
        photoUrl = photoUrl?.toString(),
        emailVerified = isEmailVerified
    )

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
