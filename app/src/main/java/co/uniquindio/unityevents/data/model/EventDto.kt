package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.EventStatus
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** DTO de `/events/{eventId}`. */
data class EventDto(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var placeName: String = "",
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var price: Long = 0L,
    var capacity: Int = 0,
    var imageUrl: String = "",
    var organizerId: String = "",
    var organizerName: String = "",
    var organizerPhotoUrl: String? = null,
    var status: String = EventStatus.PENDING.name,
    var rejectionReason: String? = null,
    @ServerTimestamp var createdAt: Date? = null,
    var attendeesCount: Int = 0
) {
    fun toDomain(): Event = Event(
        id = id,
        title = title,
        description = description,
        category = category,
        placeName = placeName,
        address = address,
        latitude = latitude,
        longitude = longitude,
        startDate = startDate,
        endDate = endDate,
        price = price,
        capacity = capacity,
        imageUrl = imageUrl,
        organizerId = organizerId,
        organizerName = organizerName,
        organizerPhotoUrl = organizerPhotoUrl,
        status = EventStatus.fromString(status),
        rejectionReason = rejectionReason,
        createdAt = createdAt,
        attendeesCount = attendeesCount
    )

    companion object {
        fun fromDomain(event: Event): EventDto = EventDto(
            id = event.id,
            title = event.title,
            description = event.description,
            category = event.category,
            placeName = event.placeName,
            address = event.address,
            latitude = event.latitude,
            longitude = event.longitude,
            startDate = event.startDate,
            endDate = event.endDate,
            price = event.price,
            capacity = event.capacity,
            imageUrl = event.imageUrl,
            organizerId = event.organizerId,
            organizerName = event.organizerName,
            organizerPhotoUrl = event.organizerPhotoUrl,
            status = event.status.name,
            rejectionReason = event.rejectionReason,
            attendeesCount = event.attendeesCount
        )
    }
}
