package co.uniquindio.unityevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import co.uniquindio.unityevents.core.navigation.AppNavigation
import co.uniquindio.unityevents.core.theme.UnityEventsTheme
import co.uniquindio.unityevents.data.repository.UserRepositoryImpl
import co.uniquindio.unityevents.domain.repository.UserRepository

/**
 * Actividad principal de la aplicacion UnityEvents.
 *
 * Es el punto de entrada de la aplicacion Android. Configura el tema
 * Material You, crea la instancia unica del repositorio de usuarios
 * (en memoria) y establece el grafo de navegacion como contenido
 * principal de Compose.
 *
 * Sigue la arquitectura de actividad unica (Single Activity) donde
 * toda la navegacion entre pantallas se maneja con Jetpack Navigation Compose.
 */
class MainActivity : ComponentActivity() {

    /** Instancia unica del repositorio de usuarios compartida en toda la app */
    private val userRepository: UserRepository = UserRepositoryImpl()

    /**
     * Metodo de creacion de la actividad.
     *
     * Configura el modo edge-to-edge para una experiencia inmersiva,
     * establece el tema Material You y monta el grafo de navegacion
     * principal de la aplicacion.
     *
     * @param savedInstanceState Estado guardado de la instancia anterior (si existe).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar el modo edge-to-edge para aprovechar toda la pantalla
        enableEdgeToEdge()

        // Establecer el contenido de Compose con el tema y la navegacion
        setContent {
            UnityEventsTheme {
                // Superficie principal que ocupa toda la pantalla con el color de fondo del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Grafo de navegacion principal con el repositorio compartido
                    AppNavigation(userRepository = userRepository)
                }
            }
        }
    }
}
