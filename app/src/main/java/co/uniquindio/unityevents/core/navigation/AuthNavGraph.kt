package co.uniquindio.unityevents.core.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import co.uniquindio.unityevents.features.login.LoginScreen
import co.uniquindio.unityevents.features.recover.RecoverPasswordScreen
import co.uniquindio.unityevents.features.register.RegisterScreen
import co.uniquindio.unityevents.features.welcome.WelcomeScreen

/**
 * Define el subgrafo de autenticacion: bienvenida → login/register/recover.
 *
 * Tras un login o registro exitoso, se invoca [onAuthenticated] que el NavHost padre
 * usa para sustituir el back stack por el grafo principal.
 */
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onAuthenticated: () -> Unit
) {
    navigation(
        route = AppDestinations.AUTH_GRAPH,
        startDestination = AppDestinations.WELCOME
    ) {
        composable(AppDestinations.WELCOME) {
            WelcomeScreen(
                onStartClick = { navController.navigate(AppDestinations.REGISTER) },
                onLoginClick = { navController.navigate(AppDestinations.LOGIN) }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = onAuthenticated,
                onNavigateToRegister = { navController.navigate(AppDestinations.REGISTER) },
                onNavigateToRecover = { navController.navigate(AppDestinations.RECOVER_PASSWORD) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = onAuthenticated,
                onNavigateToLogin = {
                    // Reemplaza el stack: desde registro → login sin dejar registro detras.
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.WELCOME)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.RECOVER_PASSWORD) {
            RecoverPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
