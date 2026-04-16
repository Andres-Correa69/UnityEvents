package co.uniquindio.unityevents.domain.model

import java.util.Date

/**
 * Notificacion para un usuario. Persistida en `/users/{uid}/notifications/{id}`.
 *
 * @property id id del documento.
 * @property type Categoria (determina el icono y la accion al tocarla).
 * @property title Titulo breve de la notificacion.
 * @property body Descripcion mas larga.
 * @property imageUrl Imagen asociada (opcional).
 * @property relatedId Id relacionado (eventId, ticketId, messageId...).
 * @property createdAt timestamp de creacion.
 * @property read true si el usuario ya la vio.
 */
data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.INFO,
    val title: String = "",
    val body: String = "",
    val imageUrl: String? = null,
    val relatedId: String? = null,
    val createdAt: Date? = null,
    val read: Boolean = false
)

/** Categorias de notificaciones soportadas. */
enum class NotificationType {
    /** Evento aprobado por moderador. */
    EVENT_APPROVED,
    /** Evento rechazado. */
    EVENT_REJECTED,
    /** Recordatorio: un evento al que asiste esta por empezar. */
    EVENT_REMINDER,
    /** Nuevo comentario en un evento del que el usuario es organizador. */
    NEW_COMMENT,
    /** Ticket comprado correctamente. */
    TICKET_PURCHASED,
    /** Mensaje general / info. */
    INFO;

    companion object {
        fun fromString(value: String?): NotificationType = values().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: INFO
    }
}
