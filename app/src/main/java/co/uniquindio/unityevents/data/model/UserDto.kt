package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.model.UserRole
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * DTO para el documento `/users/{uid}` en Firestore.
 *
 * Firestore deserializa documentos a esta clase (requiere constructor sin argumentos y
 * propiedades mutables con `var`). El campo [createdAt] se establece con `@ServerTimestamp`
 * para que Firestore asigne la hora del servidor al crear el documento.
 */
data class UserDto(
    var uid: String = "",
    var displayName: String = "",
    var email: String = "",
    var photoUrl: String? = null,
    var role: String = UserRole.USER.name,
    var level: Int = 1,
    var points: Int = 0,
    var bio: String = "",
    var city: String = "",
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) {
    /** Convierte este DTO a la entidad de dominio [User]. */
    fun toDomain(emailVerified: Boolean = false): User = User(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        emailVerified = emailVerified,
        role = UserRole.fromString(role),
        level = level,
        points = points,
        bio = bio,
        city = city
    )

    companion object {
        /** Construye un DTO a partir de una entidad de dominio, pensado para escribir en Firestore. */
        fun fromDomain(user: User): UserDto = UserDto(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl,
            role = user.role.name,
            level = user.level,
            points = user.points,
            bio = user.bio,
            city = user.city
        )
    }
}
