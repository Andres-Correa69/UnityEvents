package co.uniquindio.unityevents.data.repository

import android.net.Uri
import co.uniquindio.unityevents.core.service.ReputationService
import co.uniquindio.unityevents.data.model.EventDto
import co.uniquindio.unityevents.data.model.NotificationDto
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.EventStatus
import co.uniquindio.unityevents.domain.model.NotificationType
import co.uniquindio.unityevents.domain.repository.EventsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion de [EventsRepository] contra `/events` en Firestore + `events/` en Storage.
 */
@Singleton
class EventsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val reputationService: ReputationService
) : EventsRepository {

    private val collection get() = firestore.collection(EVENTS)

    // Nota: no usamos `orderBy` server-side en estas queries, para evitar depender de
    // indices compuestos de Firestore. Ordenamos en memoria tras recibir los resultados.
    // Si en el futuro crecen mucho (>1000 docs) conviene pasar orderBy al servidor + crear el indice.

    override fun observeApprovedEvents(): Flow<List<Event>> =
        collection
            .whereEqualTo("status", EventStatus.APPROVED.name)
            .eventsFlow()
            .map { list -> list.sortedBy { it.startDate } }

    override fun observeMyEvents(userId: String): Flow<List<Event>> =
        collection
            .whereEqualTo("organizerId", userId)
            .eventsFlow()
            .map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeEventsByStatus(status: EventStatus): Flow<List<Event>> =
        collection
            .whereEqualTo("status", status.name)
            .eventsFlow()
            .map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeEvent(eventId: String): Flow<Event?> = callbackFlow {
        val registration = collection.document(eventId).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            val dto = snap?.toObject(EventDto::class.java)
            trySend(dto?.copy(id = snap.id)?.toDomain())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun createEvent(event: Event, imageUri: Uri?): Result<String> =
        runCatching {
            // 1) Crea el documento con un id autogenerado y guarda la referencia.
            val docRef = collection.document()
            val id = docRef.id

            // 2) Si el usuario adjunto foto, subela a Storage.
            val finalImageUrl: String = if (imageUri != null) {
                val ref = storage.reference.child("events/${event.organizerId}/$id.jpg")
                ref.putFile(imageUri).await()
                ref.downloadUrl.await().toString()
            } else event.imageUrl

            // 3) Arma el DTO (forzando status = PENDING y el id).
            val dto = EventDto.fromDomain(event.copy(
                id = id,
                status = EventStatus.PENDING,
                imageUrl = finalImageUrl
            ))
            docRef.set(dto).await()
            id
        }

    override suspend fun updateStatus(
        eventId: String,
        status: EventStatus,
        rejectionReason: String?
    ): Result<Unit> = runCatching {
        // Leemos primero para conocer al organizador (lo usaremos en puntos/notificaciones).
        val snap = collection.document(eventId).get().await()
        val organizerId = snap.getString("organizerId").orEmpty()
        val title = snap.getString("title").orEmpty()

        val update = mutableMapOf<String, Any?>("status" to status.name)
        if (status == EventStatus.REJECTED) update["rejectionReason"] = rejectionReason.orEmpty()
        if (status == EventStatus.APPROVED) update["rejectionReason"] = null
        collection.document(eventId).set(update, SetOptions.merge()).await()

        // Efectos colaterales segun el nuevo estado.
        when (status) {
            EventStatus.APPROVED -> {
                // +50 al organizador.
                if (organizerId.isNotBlank()) {
                    reputationService.award(organizerId, ReputationService.Reward.EVENT_APPROVED)
                    createEventNotification(
                        userId = organizerId,
                        type = NotificationType.EVENT_APPROVED,
                        title = "Evento aprobado",
                        body = "Tu evento \"$title\" ya es visible en el feed publico.",
                        relatedId = eventId
                    )
                }
            }
            EventStatus.REJECTED -> {
                if (organizerId.isNotBlank()) {
                    createEventNotification(
                        userId = organizerId,
                        type = NotificationType.EVENT_REJECTED,
                        title = "Evento rechazado",
                        body = "Tu evento \"$title\" fue rechazado. Motivo: ${rejectionReason.orEmpty()}",
                        relatedId = eventId
                    )
                }
            }
            else -> Unit
        }
    }

    /** Crea una notificacion simple en la bandeja de un usuario. */
    private suspend fun createEventNotification(
        userId: String,
        type: NotificationType,
        title: String,
        body: String,
        relatedId: String
    ) = runCatching {
        val ref = firestore.collection("users").document(userId)
            .collection("notifications").document()
        ref.set(
            NotificationDto(
                id = ref.id,
                type = type.name,
                title = title,
                body = body,
                relatedId = relatedId,
                read = false
            )
        ).await()
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        collection.document(eventId).delete().await()
        // En Fase B no borramos las subcolecciones (Firestore no tiene cascade).
        // Un Cloud Function podria limpiarlas, pero queda fuera del alcance aqui.
    }

    private companion object {
        const val EVENTS = "events"
    }
}

/**
 * Helper privado: convierte una Query en Flow<List<Event>> via snapshot listener.
 * (Pequena extension para evitar repetir el callbackFlow en cada observe*.)
 */
private fun com.google.firebase.firestore.Query.eventsFlow(): Flow<List<Event>> = callbackFlow {
    val registration = addSnapshotListener { snap, err ->
        if (err != null) { close(err); return@addSnapshotListener }
        val list = snap?.documents.orEmpty().mapNotNull { doc ->
            doc.toObject(EventDto::class.java)?.copy(id = doc.id)?.toDomain()
        }
        trySend(list)
    }
    awaitClose { registration.remove() }
}
