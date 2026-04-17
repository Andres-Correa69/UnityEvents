package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.core.service.ReputationService
import co.uniquindio.unityevents.data.model.CommentDto
import co.uniquindio.unityevents.data.model.NotificationDto
import co.uniquindio.unityevents.domain.model.Comment
import co.uniquindio.unityevents.domain.model.NotificationType
import co.uniquindio.unityevents.domain.repository.CommentsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion de comentarios usando la subcoleccion `/events/{eventId}/comments`.
 *
 * Efectos colaterales al crear un comentario:
 *  1. Otorga +10 puntos al autor via [ReputationService].
 *  2. Si el comentario incluye una calificacion de 5 estrellas, otorga +20 puntos al
 *     organizador del evento.
 *  3. Crea una notificacion `NEW_COMMENT` en `/users/{organizerId}/notifications` para
 *     que el organizador sepa que recibio un nuevo comentario.
 */
@Singleton
class CommentsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val reputationService: ReputationService
) : CommentsRepository {

    private fun commentsRef(eventId: String) =
        firestore.collection("events").document(eventId).collection("comments")

    override fun observeComments(eventId: String): Flow<List<Comment>> = callbackFlow {
        val registration = commentsRef(eventId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(CommentDto::class.java)?.copy(id = doc.id, eventId = eventId)?.toDomain()
                }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun addComment(comment: Comment): Result<String> = runCatching {
        // 1) Guardar el comentario en Firestore. Si esto falla, abortamos.
        val ref = commentsRef(comment.eventId).document()
        val dto = CommentDto.fromDomain(comment.copy(id = ref.id))
        ref.set(dto).await()

        // Los efectos colaterales (puntos, bonus, notificaciones) NO deben abortar el
        // comentario ya guardado si alguno falla. Los envolvemos en un runCatching
        // propio (best-effort).
        runCatching {
            // 2) Leer evento para obtener al organizador (puntos + notificaciones).
            val eventSnap = firestore.collection("events").document(comment.eventId).get().await()
            val organizerId = eventSnap.getString("organizerId").orEmpty()
            val eventTitle = eventSnap.getString("title").orEmpty()

            // 3) +10 al autor del comentario.
            reputationService.award(comment.authorId, ReputationService.Reward.COMMENT_ADDED)

            // 4) Bonus +20 al organizador por calificacion de 5 estrellas (si no es el mismo).
            if (comment.rating == 5 && organizerId.isNotBlank() && organizerId != comment.authorId) {
                reputationService.award(organizerId, ReputationService.Reward.COMMENT_5_STARS)
            }

            // 5) Notificar al organizador (solo si es distinto del autor).
            if (organizerId.isNotBlank() && organizerId != comment.authorId) {
                createCommentNotification(
                    organizerId = organizerId,
                    authorName = comment.authorName.ifBlank { "Alguien" },
                    eventTitle = eventTitle,
                    eventId = comment.eventId,
                    snippet = comment.text.take(80)
                )
            }
        }

        ref.id
    }

    override suspend fun deleteComment(eventId: String, commentId: String): Result<Unit> =
        runCatching { commentsRef(eventId).document(commentId).delete().await() }

    override suspend fun flagComment(eventId: String, commentId: String): Result<Unit> =
        runCatching {
            commentsRef(eventId).document(commentId)
                .set(mapOf("flagged" to true), SetOptions.merge())
                .await()
        }

    /** Crea una notificacion tipo [NotificationType.NEW_COMMENT] en la bandeja del organizador. */
    private suspend fun createCommentNotification(
        organizerId: String,
        authorName: String,
        eventTitle: String,
        eventId: String,
        snippet: String
    ) {
        val notifRef = firestore.collection("users").document(organizerId)
            .collection("notifications").document()
        val dto = NotificationDto(
            id = notifRef.id,
            type = NotificationType.NEW_COMMENT.name,
            title = "Nuevo comentario en \"$eventTitle\"",
            body = "$authorName: \"$snippet\"",
            relatedId = eventId,
            read = false
        )
        notifRef.set(dto).await()
    }
}
