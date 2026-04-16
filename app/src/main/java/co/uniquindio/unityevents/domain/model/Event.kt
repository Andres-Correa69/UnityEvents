package co.uniquindio.unityevents.domain.model

import java.util.Date

/**
 * Evento creado por un usuario y visible (tras aprobacion) en el feed publico.
 *
 * @property id Identificador generado por Firestore.
 * @property title Titulo corto del evento (ej. "Cine foro de Kurosawa").
 * @property description Descripcion completa en Markdown / texto plano.
 * @property category Categoria: "Academico", "Cultural", "Deportivo", etc.
 * @property placeName Nombre legible del lugar (ej. "Auditorio Euclides Jaramillo").
 * @property address Direccion completa del lugar.
 * @property latitude / longitude Coordenadas geograficas (para mapa en fases posteriores).
 * @property startDate Fecha/hora de inicio.
 * @property endDate Fecha/hora de fin.
 * @property price Precio en COP; 0 significa gratis.
 * @property capacity Capacidad maxima (0 = ilimitado).
 * @property imageUrl URL publica en Firebase Storage con la imagen principal.
 * @property organizerId uid del creador.
 * @property organizerName displayName del creador en el momento de creacion.
 * @property organizerPhotoUrl foto del creador (snapshot).
 * @property status Estado del evento para el flujo de moderacion.
 * @property rejectionReason Cuando [status] es REJECTED, razon escrita por el moderador.
 * @property createdAt Timestamp de creacion.
 * @property attendeesCount Contador denormalizado de asistentes (para mostrar sin subquery).
 */
data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val placeName: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val price: Long = 0L,
    val capacity: Int = 0,
    val imageUrl: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val organizerPhotoUrl: String? = null,
    val status: EventStatus = EventStatus.PENDING,
    val rejectionReason: String? = null,
    val createdAt: Date? = null,
    val attendeesCount: Int = 0
)

/** Estado de moderacion de un evento. */
enum class EventStatus {
    /** Pendiente de revision por moderador (estado inicial al crearse). */
    PENDING,
    /** Aprobado: visible en el feed publico y se pueden comprar tickets. */
    APPROVED,
    /** Rechazado: solo lo ve el creador con la razon. */
    REJECTED;

    companion object {
        fun fromString(value: String?): EventStatus = values().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: PENDING
    }
}
