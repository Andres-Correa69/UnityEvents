package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/** CRUD de comentarios sobre eventos (`/events/{eventId}/comments`). */
interface CommentsRepository {

    /** Lista reactiva de comentarios ordenados por fecha descendente. */
    fun observeComments(eventId: String): Flow<List<Comment>>

    /** Anade un comentario. Requiere usuario autenticado (el impl lee los datos del caller). */
    suspend fun addComment(comment: Comment): Result<String>

    /** Elimina un comentario (autor o moderador). */
    suspend fun deleteComment(eventId: String, commentId: String): Result<Unit>

    /** Marca un comentario como flagged (moderador lo revisara). */
    suspend fun flagComment(eventId: String, commentId: String): Result<Unit>
}
