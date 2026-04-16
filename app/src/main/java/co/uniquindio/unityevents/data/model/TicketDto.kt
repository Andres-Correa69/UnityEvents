package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.Ticket
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** DTO de `/tickets/{ticketId}`. */
data class TicketDto(
    var id: String = "",
    var eventId: String = "",
    var eventTitle: String = "",
    var eventImageUrl: String = "",
    var eventStartDate: Date? = null,
    var userId: String = "",
    var userName: String = "",
    @ServerTimestamp var purchasedAt: Date? = null,
    var usedAt: Date? = null,
    var qrPayload: String = ""
) {
    fun toDomain(): Ticket = Ticket(
        id = id,
        eventId = eventId,
        eventTitle = eventTitle,
        eventImageUrl = eventImageUrl,
        eventStartDate = eventStartDate,
        userId = userId,
        userName = userName,
        purchasedAt = purchasedAt,
        usedAt = usedAt,
        qrPayload = qrPayload
    )

    companion object {
        fun fromDomain(t: Ticket): TicketDto = TicketDto(
            id = t.id,
            eventId = t.eventId,
            eventTitle = t.eventTitle,
            eventImageUrl = t.eventImageUrl,
            eventStartDate = t.eventStartDate,
            userId = t.userId,
            userName = t.userName,
            usedAt = t.usedAt,
            qrPayload = t.qrPayload
        )
    }
}
