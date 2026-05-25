package co.uniquindio.unityevents.core.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import co.uniquindio.unityevents.MainActivity
import co.uniquindio.unityevents.R
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servicio que conecta la app a Firebase Cloud Messaging (FCM).
 *
 * Responsabilidades:
 * 1. **Recibir tokens** (`onNewToken`): cada dispositivo recibe un token unico de FCM que
 *    el backend (Cloud Function) usa para enviarle push notifications. Cuando cambia,
 *    lo guardamos en `users/{uid}.fcmTokens` (array, para soportar multi-dispositivo).
 * 2. **Recibir mensajes** (`onMessageReceived`): cuando llega un push con la app cerrada
 *    o en background, Android lo dibuja automaticamente. Pero si la app esta en foreground
 *    el sistema NO la muestra — debemos construirla manualmente aqui.
 *
 * Las notificaciones se enrutan al [MainActivity]; el manejo del deep-link (ir al detalle
 * del evento, etc.) se hace via los `data` del mensaje.
 */
@AndroidEntryPoint
class UnityEventsFcmService : FirebaseMessagingService() {

    @Inject lateinit var profileRepository: ProfileRepository

    // Scope propio del Service para no bloquear el dispatcher principal de FCM.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Llamado cada vez que FCM genera o rota el token de este dispositivo.
     * Lo persistimos en Firestore (idempotente via arrayUnion).
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Nuevo FCM token recibido: ${token.take(16)}...")
        scope.launch {
            profileRepository.saveFcmToken(token)
                .onFailure { Log.w(TAG, "No se pudo guardar el FCM token", it) }
        }
    }

    /**
     * Llamado cuando llega un mensaje con la app en foreground.
     * Construimos la notificacion manualmente para que se muestre.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notif = message.notification
        val title = notif?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = notif?.body ?: message.data["body"].orEmpty()
        Log.i(TAG, "Push recibido en foreground: title=$title")
        showNotification(title = title, body = body)
    }

    /** Construye y muestra la notificacion sobre el canal por defecto. */
    private fun showNotification(title: String, body: String) {
        val channelId = getString(R.string.fcm_default_channel_id)

        // Intent que lleva a MainActivity al tocar la notif. (En el futuro, podriamos leer
        // el campo `relatedId` del mensaje y abrir la pantalla especifica del evento.)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private companion object {
        const val TAG = "FcmService"
    }
}
