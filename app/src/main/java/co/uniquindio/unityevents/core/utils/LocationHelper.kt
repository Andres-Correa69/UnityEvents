package co.uniquindio.unityevents.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Coordenadas geograficas simples. */
data class LatLngCoords(val lat: Double, val lng: Double)

/**
 * Helper que envuelve el `FusedLocationProviderClient` de Play Services.
 *
 * La UI debe solicitar y confirmar el permiso [Manifest.permission.ACCESS_FINE_LOCATION]
 * (o COARSE) ANTES de llamar a [getCurrentLocation]; si no, [getCurrentLocation]
 * devuelve null.
 */
@Singleton
class LocationHelper @Inject constructor() {

    /**
     * Obtiene la ubicacion actual del dispositivo. Prefiere precision alta para picking
     * de eventos. Devuelve null si no hay permiso, no hay GPS activo o hay timeout.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LatLngCoords? {
        if (!hasLocationPermission(context)) return null

        val client = LocationServices.getFusedLocationProviderClient(context)
        // `getCurrentLocation` es mejor que `lastLocation` porque fuerza una lectura fresca.
        val location = try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (_: SecurityException) {
            return null
        } catch (_: Exception) {
            return null
        }
        return location?.let { LatLngCoords(it.latitude, it.longitude) }
    }

    /** Confirma que el usuario ya otorgo el permiso de ubicacion (fino o aproximado). */
    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}
