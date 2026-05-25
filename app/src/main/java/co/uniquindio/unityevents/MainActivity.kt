package co.uniquindio.unityevents

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import co.uniquindio.unityevents.core.navigation.AppNavHost
import co.uniquindio.unityevents.core.theme.UnityEventsTheme
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Activity principal y unica de UnityEvents. Carga el tema de Compose y entrega el control
 * a [AppNavHost], que decide el grafo de navegacion inicial segun haya sesion o no.
 *
 * `@AndroidEntryPoint` permite que Hilt inyecte dependencias via `@Inject lateinit var`.
 */
@AndroidEntryPoint
class  MainActivity : ComponentActivity() {

    /** Cliente de Firebase Auth (proporcionado por `AppModule`). Usado para saber si hay sesion activa. */
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    /** Repositorio del perfil — se usa aqui solo para guardar el FCM token al iniciar. */
    @Inject
    lateinit var profileRepository: ProfileRepository

    /**
     * Launcher para pedir el permiso POST_NOTIFICATIONS (obligatorio en Android 13+).
     * Si el usuario lo concede, FCM podra mostrar push notifications; si no, no.
     */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.i(TAG, "Permiso de notificaciones ${if (granted) "concedido" else "denegado"}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen: muestra el logo sobre fondo blanco mientras Application + Hilt
        // terminan de inicializar. Se queda hasta que la Activity esta lista para dibujar.
        // DEBE invocarse ANTES de super.onCreate() segun la documentacion oficial.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Contenido edge-to-edge: la UI dibuja tras las barras de sistema, Compose gestiona los insets.
        enableEdgeToEdge()

        // Captura estado inicial de sesion. Los cambios posteriores se manejan via navegacion
        // (login/registro exitoso -> navigate MAIN_GRAPH; signOut -> navigate AUTH_GRAPH).
        val isUserLoggedIn = firebaseAuth.currentUser != null

        // Setup de push notifications: pedir permiso (Android 13+) y registrar el token
        // actual del dispositivo asociandolo al usuario logueado. UnityEventsFcmService
        // se encarga del refresh automatico via onNewToken cuando el token rota.
        requestNotificationPermissionIfNeeded()
        if (isUserLoggedIn) registerCurrentFcmToken()

        setContent {
            UnityEventsTheme {
                AppNavHost(isUserLoggedIn = isUserLoggedIn)
            }
        }
    }

    /** En Android 13+ POST_NOTIFICATIONS es runtime permission; antes era implicito. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val already = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!already) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Lee el FCM token actual del dispositivo y lo guarda en el array `fcmTokens` del
     * usuario en Firestore. Es idempotente: si ya estaba guardado, no se duplica.
     */
    private fun registerCurrentFcmToken() {
        lifecycleScope.launch {
            runCatching { FirebaseMessaging.getInstance().token.await() }
                .onSuccess { token ->
                    Log.i(TAG, "FCM token actual: ${token.take(16)}...")
                    profileRepository.saveFcmToken(token)
                }
                .onFailure { Log.w(TAG, "No se pudo obtener el FCM token", it) }
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
