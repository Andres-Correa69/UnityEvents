package co.uniquindio.unityevents.domain.model

import java.util.Date

/**
 * Ticket digital que un usuario obtiene al inscribirse a un evento.
 * Persistido en `/tickets/{ticketId}`.
 *
 * @property id identificador generado por Firestore (se codifica en el QR).
 * @property eventId id del evento al que da acceso.
 * @property eventTitle snapshot del titulo (para mostrar sin subquery).
 * @property eventImageUrl snapshot de la imagen del evento.
 * @property eventStartDate snapshot de la fecha de inicio.
 * @property userId uid del propietario.
 * @property userName snapshot del nombre del usuario (para escaneo).
 * @property purchasedAt timestamp de compra/obtencion.
 * @property usedAt timestamp de uso (null hasta que el moderador lo escanee).
 * @property qrPayload payload que se codifica en el QR; tipicamente el id del ticket.
 */
data class Ticket(
    val id: String = "",
    val eventId: String = "",
    val eventTitle: String = "",
    val eventImageUrl: String = "",
    val eventStartDate: Date? = null,
    val userId: String = "",
    val userName: String = "",
    val purchasedAt: Date? = null,
    val usedAt: Date? = null,
    val qrPayload: String = ""
) {
    /** Indica si el ticket ya fue canjeado (usado en la entrada del evento). */
    val isUsed: Boolean get() = usedAt != null
}
