package co.uniquindio.unityevents.domain.model

import java.util.Date

/**
 * Comentario y calificacion de un usuario sobre un evento.
 * Persistido en `/events/{eventId}/comments/{commentId}`.
 *
 * @property id identificador generado por Firestore.
 * @property eventId id del evento comentado.
 * @property authorId uid del autor.
 * @property authorName displayName snapshot.
 * @property authorPhotoUrl foto snapshot del autor.
 * @property text Contenido del comentario.
 * @property rating Calificacion de 1..5 estrellas; 0 si el usuario no quiso puntuar.
 * @property createdAt Timestamp de creacion.
 * @property flagged Marca moderadora: true si un moderador lo marco como inapropiado.
 */
data class Comment(
    val id: String = "",
    val eventId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String? = null,
    val text: String = "",
    val rating: Int = 0,
    val createdAt: Date? = null,
    val flagged: Boolean = false
)
