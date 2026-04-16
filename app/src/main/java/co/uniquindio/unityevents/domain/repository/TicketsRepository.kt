package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.Ticket
import kotlinx.coroutines.flow.Flow

/** CRUD de tickets (`/tickets`). */
interface TicketsRepository {

    /** Lista de tickets del usuario actual, ordenada por fecha de evento. */
    fun observeMyTickets(userId: String): Flow<List<Ticket>>

    /** Detalle de un ticket por id. */
    suspend fun getTicket(ticketId: String): Result<Ticket?>

    /**
     * Compra (crea) un ticket a partir de un evento. En Fase B solo creamos el documento;
     * no hay pasarela de pago real (price = 0 en la mayoria de eventos universitarios).
     */
    suspend fun purchaseTicket(event: Event, userId: String, userName: String): Result<String>

    /** Marca un ticket como usado (lo hace un moderador al escanear el QR en la entrada). */
    suspend fun markTicketUsed(ticketId: String): Result<Ticket>
}
