package co.uniquindio.unityevents.core.navigation

/**
 * Identificadores (rutas) usadas por Navigation Compose.
 *
 * Centralizados aqui para evitar cadenas mageas dispersas por el codigo y facilitar
 * refactors. Cada objeto representa una pantalla o un subgrafo.
 */
object AppDestinations {

    // --- Subgrafos ---
    /** Subgrafo de autenticacion (bienvenida, login, registro, recuperar). */
    const val AUTH_GRAPH = "auth_graph"

    /** Subgrafo principal (post-autenticacion). */
    const val MAIN_GRAPH = "main_graph"

    // --- Pantallas de autenticacion ---
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER_PASSWORD = "recover_password"

    // --- Pantallas principales (placeholder en Fase A) ---
    const val HOME = "home"
}
