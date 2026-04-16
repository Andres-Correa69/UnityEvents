package co.uniquindio.unityevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import co.uniquindio.unityevents.features.home.HomePlaceholderScreen

/**
 * Composable raiz de navegacion. Decide el grafo inicial segun si el usuario esta autenticado.
 *
 * @param isUserLoggedIn true si `FirebaseAuth.currentUser != null` al arrancar la app.
 */
@Composable
fun AppNavHost(isUserLoggedIn: Boolean) {
    val navController = rememberNavController()

    // Grafo inicial: si hay sesion, entra al main; si no, al de autenticacion.
    val startGraph = if (isUserLoggedIn) AppDestinations.MAIN_GRAPH else AppDestinations.AUTH_GRAPH

    NavHost(
        navController = navController,
        startDestination = startGraph
    ) {
        // Grafo de autenticacion: al autenticarse salta al grafo principal limpiando el stack.
        authGraph(
            navController = navController,
            onAuthenticated = {
                navController.navigate(AppDestinations.MAIN_GRAPH) {
                    popUpTo(AppDestinations.AUTH_GRAPH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        // Grafo principal (placeholder en Fase A).
        mainGraph(
            onSignedOut = {
                navController.navigate(AppDestinations.AUTH_GRAPH) {
                    popUpTo(AppDestinations.MAIN_GRAPH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

/**
 * Subgrafo principal. En Fase A solo contiene `HomePlaceholderScreen`; en Fase B se expande
 * con Mapa de Eventos, Detalle, Ticket, etc.
 */
private fun NavGraphBuilder.mainGraph(
    onSignedOut: () -> Unit
) {
    navigation(
        route = AppDestinations.MAIN_GRAPH,
        startDestination = AppDestinations.HOME
    ) {
        composable(AppDestinations.HOME) {
            HomePlaceholderScreen(onSignOut = onSignedOut)
        }
    }
}
