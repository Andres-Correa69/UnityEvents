package co.uniquindio.unityevents.domain.model

/**
 * Entidad de dominio que representa a un usuario autenticado de UnityEvents.
 *
 * Esta clase vive en la capa de dominio y NO depende de Firebase ni de DTOs.
 * Los repositorios convierten los objetos externos (FirebaseUser, UserDto de Firestore)
 * a este modelo antes de exponerlos a los ViewModels.
 *
 * @property uid Identificador unico del usuario (Firebase Auth UID).
 * @property displayName Nombre visible del usuario; puede estar vacio en Google Sign-In sin perfil completo.
 * @property email Correo electronico del usuario.
 * @property photoUrl URL de la foto de perfil; null si no tiene una definida.
 * @property emailVerified Indica si el correo ha sido verificado por el usuario.
 */
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val emailVerified: Boolean = false
)
