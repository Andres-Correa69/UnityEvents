package co.uniquindio.unityevents.domain.repository

import android.net.Uri
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.EventStatus
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para el CRUD de eventos contra Firestore + Storage.
 *
 * Todas las consultas de lectura son reactivas ([Flow]) para actualizar la UI en vivo.
 * Las operaciones de escritura devuelven [Result] con el id del documento o error.
 */
interface EventsRepository {

    /** Eventos APROBADOS ordenados por fecha de inicio ascendente (feed publico). */
    fun observeApprovedEvents(): Flow<List<Event>>

    /** Todos los eventos del usuario actual (cualquier estado), para la pantalla "Mis eventos". */
    fun observeMyEvents(userId: String): Flow<List<Event>>

    /** Eventos filtrados por estado — usado por el panel de moderacion. */
    fun observeEventsByStatus(status: EventStatus): Flow<List<Event>>

    /** Lee un evento individual, reactivo a cambios (aprobaciones, ediciones, conteo, etc.). */
    fun observeEvent(eventId: String): Flow<Event?>

    /**
     * Crea un evento. Si [imageUri] != null, primero sube la imagen a
     * `/events/{uid}/{eventId}.jpg` en Storage y guarda la URL en el doc.
     * El evento se crea siempre con status PENDING — un moderador debe aprobarlo.
     *
     * @return `Result.success(eventId)` o `Result.failure` con la causa.
     */
    suspend fun createEvent(event: Event, imageUri: Uri?): Result<String>

    /** Cambia el estado de un evento (para moderadores). */
    suspend fun updateStatus(eventId: String, status: EventStatus, rejectionReason: String? = null): Result<Unit>

    /** Elimina un evento y todas sus subcolecciones (comentarios). */
    suspend fun deleteEvent(eventId: String): Result<Unit>
}
