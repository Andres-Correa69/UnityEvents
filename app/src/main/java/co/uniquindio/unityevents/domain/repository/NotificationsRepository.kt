package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/** CRUD de notificaciones del usuario (`/users/{uid}/notifications`). */
interface NotificationsRepository {

    /** Notificaciones del usuario, mas recientes primero. */
    fun observeNotifications(userId: String): Flow<List<Notification>>

    /** Cuenta de notificaciones no leidas (para badge del icono de campana). */
    fun observeUnreadCount(userId: String): Flow<Int>

    /** Marca como leida. */
    suspend fun markAsRead(userId: String, notificationId: String): Result<Unit>

    /** Marca todas como leidas. */
    suspend fun markAllAsRead(userId: String): Result<Unit>

    /** Crea una notificacion (llamado por el sistema tras eventos de dominio). */
    suspend fun createNotification(userId: String, notification: Notification): Result<String>
}
