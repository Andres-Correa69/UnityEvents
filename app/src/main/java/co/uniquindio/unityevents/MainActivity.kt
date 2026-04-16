package co.uniquindio.unityevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import co.uniquindio.unityevents.core.navigation.AppNavHost
import co.uniquindio.unityevents.core.theme.UnityEventsTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity principal y unica de UnityEvents. Carga el tema de Compose y entrega el control
 * a [AppNavHost], que decide el grafo de navegacion inicial segun haya sesion o no.
 *
 * `@AndroidEntryPoint` permite que Hilt inyecte dependencias via `@Inject lateinit var`.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Cliente de Firebase Auth (proporcionado por `AppModule`). Usado para saber si hay sesion activa. */
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Contenido edge-to-edge: la UI dibuja tras las barras de sistema, Compose gestiona los insets.
        enableEdgeToEdge()

        // Captura estado inicial de sesion. Los cambios posteriores se manejan via navegacion
        // (login/registro exitoso -> navigate MAIN_GRAPH; signOut -> navigate AUTH_GRAPH).
        val isUserLoggedIn = firebaseAuth.currentUser != null

        setContent {
            UnityEventsTheme {
                AppNavHost(isUserLoggedIn = isUserLoggedIn)
            }
        }
    }
}
