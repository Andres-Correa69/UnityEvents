package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.TicketDto
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.Ticket
import co.uniquindio.unityevents.domain.repository.TicketsRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/** Implementacion de [TicketsRepository] contra `/tickets`. */
@Singleton
class TicketsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TicketsRepository {

    private val collection get() = firestore.collection(TICKETS)

    override fun observeMyTickets(userId: String): Flow<List<Ticket>> = callbackFlow {
        // Sin orderBy server-side para no depender de indice compuesto (userId + eventStartDate).
        // Ordenamos por fecha del evento (ascendente) al recibir los documentos.
        val registration = collection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty()
                    .mapNotNull { doc -> doc.toObject(TicketDto::class.java)?.copy(id = doc.id)?.toDomain() }
                    .sortedBy { it.eventStartDate }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getTicket(ticketId: String): Result<Ticket?> = runCatching {
        val snap = collection.document(ticketId).get().await()
        snap.toObject(TicketDto::class.java)?.copy(id = snap.id)?.toDomain()
    }

    override suspend fun purchaseTicket(
        event: Event,
        userId: String,
        userName: String
    ): Result<String> = runCatching {
        val ref = collection.document()
        val ticket = Ticket(
            id = ref.id,
            eventId = event.id,
            eventTitle = event.title,
            eventImageUrl = event.imageUrl,
            eventStartDate = event.startDate,
            userId = userId,
            userName = userName,
            qrPayload = ref.id   // el payload es el propio id del ticket (unico)
        )
        ref.set(TicketDto.fromDomain(ticket)).await()

        // Incrementa el contador denormalizado del evento (para mostrar "X asistentes").
        firestore.collection("events").document(event.id)
            .set(mapOf("attendeesCount" to FieldValue.increment(1)), SetOptions.merge())
            .await()

        ref.id
    }

    override suspend fun markTicketUsed(ticketId: String): Result<Ticket> = runCatching {
        val docRef = collection.document(ticketId)
        val snap = docRef.get().await()
        val existing = snap.toObject(TicketDto::class.java)
            ?: error("El ticket $ticketId no existe.")

        if (existing.usedAt != null) {
            // Ya usado: devolvemos el estado actual sin modificar (el caller avisa al moderador).
            return@runCatching existing.copy(id = snap.id).toDomain()
        }

        val now = Date()
        docRef.set(mapOf("usedAt" to now), SetOptions.merge()).await()
        existing.copy(id = snap.id, usedAt = now).toDomain()
    }

    private companion object {
        const val TICKETS = "tickets"
    }
}
