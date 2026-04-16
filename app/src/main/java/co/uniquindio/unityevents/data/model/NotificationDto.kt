package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.Notification
import co.uniquindio.unityevents.domain.model.NotificationType
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** DTO de `/users/{uid}/notifications/{id}`. */
data class NotificationDto(
    var id: String = "",
    var type: String = NotificationType.INFO.name,
    var title: String = "",
    var body: String = "",
    var imageUrl: String? = null,
    var relatedId: String? = null,
    @ServerTimestamp var createdAt: Date? = null,
    var read: Boolean = false
) {
    fun toDomain(): Notification = Notification(
        id = id,
        type = NotificationType.fromString(type),
        title = title,
        body = body,
        imageUrl = imageUrl,
        relatedId = relatedId,
        createdAt = createdAt,
        read = read
    )

    companion object {
        fun fromDomain(n: Notification): NotificationDto = NotificationDto(
            id = n.id,
            type = n.type.name,
            title = n.title,
            body = n.body,
            imageUrl = n.imageUrl,
            relatedId = n.relatedId,
            read = n.read
        )
    }
}
