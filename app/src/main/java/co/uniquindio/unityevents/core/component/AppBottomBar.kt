package co.uniquindio.unityevents.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import co.uniquindio.unityevents.core.navigation.AppDestinations

/**
 * Barra inferior de navegacion. Visible solo en las 4 pantallas principales
 * (Home, Mis tickets, Notificaciones, Perfil).
 *
 * Usa `launchSingleTop = true` y `popUpTo` al start de main para evitar apilar el mismo
 * destino al tocar varias veces el mismo icono.
 */
@Composable
fun AppBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    unreadNotifications: Int = 0
) {
    val items = listOf(
        BottomItem(AppDestinations.HOME, "Inicio", Icons.Filled.Home),
        BottomItem(AppDestinations.MY_TICKETS, "Tickets", Icons.Filled.ConfirmationNumber),
        BottomItem(AppDestinations.NOTIFICATIONS, "Alertas", Icons.Filled.Notifications, badgeCount = unreadNotifications),
        BottomItem(AppDestinations.PROFILE, "Perfil", Icons.Filled.Person)
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(AppDestinations.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (item.badgeCount > 0) {
                        BadgedBox(badge = { Badge { Text("${item.badgeCount}") } }) {
                            Icon(item.icon, contentDescription = item.label)
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label)
                    }
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

/** Data class privada para la configuracion de cada entrada del bottom bar. */
private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)
