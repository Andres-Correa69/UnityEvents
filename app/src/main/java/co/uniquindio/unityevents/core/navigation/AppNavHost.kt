package co.uniquindio.unityevents.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import co.uniquindio.unityevents.core.component.AppBottomBar
import co.uniquindio.unityevents.features.events.CreateEventScreen
import co.uniquindio.unityevents.features.events.EventDetailScreen
import co.uniquindio.unityevents.features.home.HomeScreen
import co.uniquindio.unityevents.features.moderation.ModerationListScreen
import co.uniquindio.unityevents.features.moderation.ModeratorDashboardScreen
import co.uniquindio.unityevents.features.moderation.ReportsListScreen
import co.uniquindio.unityevents.features.notifications.NotificationsScreen
import co.uniquindio.unityevents.features.profile.EditProfileScreen
import co.uniquindio.unityevents.features.profile.LevelsScreen
import co.uniquindio.unityevents.features.profile.ProfileScreen
import co.uniquindio.unityevents.features.profile.SettingsScreen
import co.uniquindio.unityevents.features.tickets.MyTicketsScreen
import co.uniquindio.unityevents.features.tickets.QrScannerScreen
import co.uniquindio.unityevents.features.tickets.TicketDigitalScreen

/**
 * Composable raiz de navegacion. Decide el grafo inicial segun autenticacion y gestiona
 * la visibilidad del bottom bar: solo aparece en los 4 tabs principales de MAIN_GRAPH.
 */
@Composable
fun AppNavHost(isUserLoggedIn: Boolean) {
    val navController = rememberNavController()
    val startGraph = if (isUserLoggedIn) AppDestinations.MAIN_GRAPH else AppDestinations.AUTH_GRAPH

    // Rastrea la ruta actual para saber si mostrar el bottom bar.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomBarRoutes = setOf(
        AppDestinations.HOME,
        AppDestinations.MY_TICKETS,
        AppDestinations.NOTIFICATIONS,
        AppDestinations.PROFILE
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    // Contador de notificaciones no leidas (alimenta el badge de la barra).
    val badgeViewModel: BottomBarBadgeViewModel = hiltViewModel()
    val unread by badgeViewModel.unread.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    unreadNotifications = unread
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startGraph,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Grafo de autenticacion (reuso del existente) ---
            authGraph(
                navController = navController,
                onAuthenticated = {
                    navController.navigate(AppDestinations.MAIN_GRAPH) {
                        popUpTo(AppDestinations.AUTH_GRAPH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            // --- Grafo principal con bottom nav ---
            mainGraph(
                navController = navController,
                onSignedOut = {
                    navController.navigate(AppDestinations.AUTH_GRAPH) {
                        popUpTo(AppDestinations.MAIN_GRAPH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/**
 * Define el subgrafo MAIN_GRAPH: las 4 tabs con bottom bar + todos los destinos full-screen
 * que se acceden desde ellas (detalle de evento, crear, ticket, perfil, moderacion...).
 */
private fun NavGraphBuilder.mainGraph(
    navController: androidx.navigation.NavHostController,
    onSignedOut: () -> Unit
) {
    navigation(
        route = AppDestinations.MAIN_GRAPH,
        startDestination = AppDestinations.HOME
    ) {
        // --- Bottom nav tabs ---
        composable(AppDestinations.HOME) {
            HomeScreen(
                onEventClick = { id -> navController.navigate(AppDestinations.eventDetail(id)) },
                onCreateEventClick = { navController.navigate(AppDestinations.CREATE_EVENT) }
            )
        }
        composable(AppDestinations.MY_TICKETS) {
            MyTicketsScreen(
                onTicketClick = { id -> navController.navigate(AppDestinations.ticketDigital(id)) }
            )
        }
        composable(AppDestinations.NOTIFICATIONS) {
            NotificationsScreen()
        }
        composable(AppDestinations.PROFILE) {
            ProfileScreen(
                onEditProfile = { navController.navigate(AppDestinations.EDIT_PROFILE) },
                onSettings = { navController.navigate(AppDestinations.SETTINGS) },
                onLevels = { navController.navigate(AppDestinations.LEVELS) },
                onModeratorDashboard = { navController.navigate(AppDestinations.MODERATOR_DASHBOARD) }
            )
        }

        // --- Detalle / creacion de eventos ---
        composable(
            route = AppDestinations.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStack ->
            val eventId = backStack.arguments?.getString("eventId").orEmpty()
            EventDetailScreen(
                onBack = { navController.popBackStack() },
                onTicketPurchased = { ticketId ->
                    navController.navigate(AppDestinations.ticketDigital(ticketId)) {
                        popUpTo(AppDestinations.HOME)
                    }
                },
                onScanTickets = { navController.navigate(AppDestinations.qrScanner(eventId)) }
            )
        }
        composable(AppDestinations.CREATE_EVENT) {
            CreateEventScreen(
                onBack = { navController.popBackStack() },
                onCreated = { eventId ->
                    navController.navigate(AppDestinations.eventDetail(eventId)) {
                        popUpTo(AppDestinations.HOME)
                    }
                }
            )
        }

        // --- Tickets ---
        composable(
            route = AppDestinations.TICKET_DIGITAL,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) {
            TicketDigitalScreen(onBack = { navController.popBackStack() })
        }

        // --- Perfil ---
        composable(AppDestinations.EDIT_PROFILE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(AppDestinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() }, onSignedOut = onSignedOut)
        }
        composable(AppDestinations.LEVELS) {
            LevelsScreen(onBack = { navController.popBackStack() })
        }

        // --- Moderacion ---
        composable(AppDestinations.MODERATOR_DASHBOARD) {
            ModeratorDashboardScreen(
                onBack = { navController.popBackStack() },
                onOpenList = { filter -> navController.navigate(AppDestinations.moderationList(filter)) },
                onOpenReports = { navController.navigate(AppDestinations.REPORTS_LIST) }
            )
        }
        composable(
            route = AppDestinations.MODERATION_LIST,
            arguments = listOf(navArgument("filter") { type = NavType.StringType })
        ) {
            ModerationListScreen(
                onBack = { navController.popBackStack() },
                onEventClick = { id -> navController.navigate(AppDestinations.eventDetail(id)) }
            )
        }
        composable(AppDestinations.REPORTS_LIST) {
            ReportsListScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = AppDestinations.QR_SCANNER,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            QrScannerScreen(onBack = { navController.popBackStack() })
        }
    }
}
