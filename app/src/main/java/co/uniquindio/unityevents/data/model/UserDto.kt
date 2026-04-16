package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.User
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * DTO para el documento `/users/{uid}` en Firestore.
 *
 * Firestore deserializa documentos a esta clase (requiere constructor sin argumentos y
 * propiedades mutables con `var`). El campo [createdAt] se establece con `@ServerTimestamp`
 * para que Firestore asigne la hora del servidor al crear el documento.
 *
 * Las funciones [toDomain] y [fromDomain] convierten entre este DTO y la entidad de dominio.
 */
data class UserDto(
    var uid: String = "",
    var displayName: String = "",
    var email: String = "",
    var photoUrl: String? = null,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) {
    /** Convierte este DTO a la entidad de dominio [User]. */
    fun toDomain(emailVerified: Boolean = false): User = User(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl,
        emailVerified = emailVerified
    )

    companion object {
        /** Construye un DTO a partir de una entidad de dominio, pensado para escribir en Firestore. */
        fun fromDomain(user: User): UserDto = UserDto(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email,
            photoUrl = user.photoUrl
        )
    }
}
