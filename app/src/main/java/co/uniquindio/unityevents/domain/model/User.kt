package co.uniquindio.unityevents.domain.model

/**
 * Entidad de dominio que representa a un usuario autenticado de UnityEvents.
 *
 * Combina informacion de Firebase Auth (uid, email, emailVerified) con datos del documento
 * `/users/{uid}` en Firestore (role, level, points, bio, city).
 *
 * @property uid Identificador unico (Firebase Auth UID).
 * @property displayName Nombre visible del usuario.
 * @property email Correo electronico.
 * @property photoUrl URL de la foto de perfil; null si no tiene.
 * @property emailVerified Si el correo ha sido verificado.
 * @property role Rol del usuario (USER por defecto, MODERATOR para moderar contenido).
 * @property level Nivel del sistema de reputacion (1..5, basado en puntos acumulados).
 * @property points Puntos totales acumulados por asistir/crear eventos aprobados.
 * @property bio Descripcion libre del usuario (editable desde el perfil).
 * @property city Ciudad del usuario.
 */
data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val emailVerified: Boolean = false,
    val role: UserRole = UserRole.USER,
    val level: Int = 1,
    val points: Int = 0,
    val bio: String = "",
    val city: String = ""
)

/** Rol del usuario dentro de la app: determina que features puede ver (moderacion, etc.). */
enum class UserRole {
    /** Usuario estandar. */
    USER,
    /** Moderador: accede al panel de moderacion y aprueba/rechaza eventos y comentarios. */
    MODERATOR,
    /** Administrador: puede cambiar roles y configurar la app. */
    ADMIN;

    companion object {
        /** Convierte una cadena (viene de Firestore) al enum; cae a USER si no matchea. */
        fun fromString(value: String?): UserRole = values().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: USER
    }
}
