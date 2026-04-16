package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.Comment
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** DTO de `/events/{eventId}/comments/{commentId}`. */
data class CommentDto(
    var id: String = "",
    var eventId: String = "",
    var authorId: String = "",
    var authorName: String = "",
    var authorPhotoUrl: String? = null,
    var text: String = "",
    var rating: Int = 0,
    @ServerTimestamp var createdAt: Date? = null,
    var flagged: Boolean = false
) {
    fun toDomain(): Comment = Comment(
        id = id,
        eventId = eventId,
        authorId = authorId,
        authorName = authorName,
        authorPhotoUrl = authorPhotoUrl,
        text = text,
        rating = rating,
        createdAt = createdAt,
        flagged = flagged
    )

    companion object {
        fun fromDomain(c: Comment): CommentDto = CommentDto(
            id = c.id,
            eventId = c.eventId,
            authorId = c.authorId,
            authorName = c.authorName,
            authorPhotoUrl = c.authorPhotoUrl,
            text = c.text,
            rating = c.rating,
            flagged = c.flagged
        )
    }
}
