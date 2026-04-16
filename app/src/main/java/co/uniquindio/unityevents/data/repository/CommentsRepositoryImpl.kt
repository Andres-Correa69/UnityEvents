package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.CommentDto
import co.uniquindio.unityevents.domain.model.Comment
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

/** Implementacion de comentarios usando la subcoleccion `/events/{eventId}/comments`. */
@Singleton
class CommentsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
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
        val ref = commentsRef(comment.eventId).document()
        val dto = CommentDto.fromDomain(comment.copy(id = ref.id))
        ref.set(dto).await()
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
}
