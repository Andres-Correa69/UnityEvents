package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.NotificationDto
import co.uniquindio.unityevents.domain.model.Notification
import co.uniquindio.unityevents.domain.repository.NotificationsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Impl de notificaciones — vivien en `/users/{uid}/notifications`. */
@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationsRepository {

    private fun ref(userId: String) =
        firestore.collection("users").document(userId).collection("notifications")

    override fun observeNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val registration = ref(userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(NotificationDto::class.java)?.copy(id = doc.id)?.toDomain()
                }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun observeUnreadCount(userId: String): Flow<Int> = callbackFlow {
        val registration = ref(userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.size() ?: 0)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun markAsRead(userId: String, notificationId: String): Result<Unit> =
        runCatching {
            ref(userId).document(notificationId)
                .set(mapOf("read" to true), SetOptions.merge())
                .await()
        }

    override suspend fun markAllAsRead(userId: String): Result<Unit> = runCatching {
        val pending = ref(userId).whereEqualTo("read", false).get().await()
        val batch = firestore.batch()
        pending.documents.forEach { doc ->
            batch.set(doc.reference, mapOf("read" to true), SetOptions.merge())
        }
        batch.commit().await()
    }

    override suspend fun createNotification(
        userId: String,
        notification: Notification
    ): Result<String> = runCatching {
        val docRef = ref(userId).document()
        docRef.set(NotificationDto.fromDomain(notification.copy(id = docRef.id))).await()
        docRef.id
    }
}
