package co.uniquindio.unityevents

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase [Application] de UnityEvents.
 *
 * La anotacion `@HiltAndroidApp` detona la generacion del contenedor de dependencias
 * a nivel de aplicacion; a partir de aqui las Activities/Fragments/ViewModels marcados
 * con `@AndroidEntryPoint` o `@HiltViewModel` pueden recibir sus dependencias.
 */
@HiltAndroidApp
class UnityEventsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Notification channel obligatorio desde Android 8 (API 26) para mostrar pushes.
        // Crear el canal aqui (al inicio de Application) garantiza que existe ANTES de
        // que FCM intente publicar la primera notificacion.
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            getString(R.string.fcm_default_channel_id),
            getString(R.string.fcm_default_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.fcm_default_channel_description)
            enableLights(true)
        }
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
