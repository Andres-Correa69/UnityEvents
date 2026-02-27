package co.uniquindio.unityevents.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.uniquindio.unityevents.domain.repository.UserRepository
import co.uniquindio.unityevents.features.forgotpassword.ForgotPasswordScreen
import co.uniquindio.unityevents.features.home.HomeScreen
import co.uniquindio.unityevents.features.login.LoginScreen
import co.uniquindio.unityevents.features.recoverpassword.RecoverPasswordScreen
import co.uniquindio.unityevents.features.register.RegisterScreen
import co.uniquindio.unityevents.features.splash.SplashScreen

/**
 * Rutas de navegacion de la aplicacion.
 *
 * Define las constantes de las rutas utilizadas en el grafo de navegacion
 * para evitar errores de tipeo y centralizar la definicion de rutas.
 */
object NavigationRoutes {
    /** Ruta de la pantalla de splash/bienvenida (pantalla inicial) */
    const val SPLASH = "splash"

    /** Ruta de la pantalla de inicio de sesion */
    const val LOGIN = "login"

    /** Ruta de la pantalla de registro de usuario */
    const val REGISTER = "register"

    /** Ruta de la pantalla principal (home) */
    const val HOME = "home"

    /** Ruta de la pantalla de olvido de contrasena */
    const val FORGOT_PASSWORD = "forgot_password"

    /** Ruta de la pantalla de recuperacion de contrasena con argumento email */
    const val RECOVER_PASSWORD = "recover_password/{email}"

    /**
     * Construye la ruta de recuperacion de contrasena con el email como argumento.
     *
     * @param email Correo electronico del usuario para la recuperacion.
     * @return Ruta completa con el email codificado.
     */
    fun recoverPasswordRoute(email: String): String = "recover_password/$email"
}

/**
 * Grafo de navegacion principal de la aplicacion UnityEvents.
 *
 * Define todas las pantallas de la aplicacion y las transiciones
 * entre ellas. Utiliza Jetpack Navigation Compose para gestionar
 * la pila de navegacion.
 *
 * Flujo de navegacion:
 * - Splash (inicio) -> Login (despues de la animacion)
 * - Login -> Home (login exitoso)
 * - Login -> Register (crear cuenta)
 * - Login -> ForgotPassword (olvido de contrasena)
 * - Register -> Login (registro exitoso o volver)
 * - ForgotPassword -> RecoverPassword (codigo enviado)
 * - RecoverPassword -> Login (contrasena restablecida)
 * - Home -> Login (cerrar sesion)
 *
 * @param navController Controlador de navegacion, por defecto se crea uno nuevo.
 * @param userRepository Repositorio de usuarios compartido entre todas las pantallas.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    userRepository: UserRepository
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.SPLASH
    ) {
        // --- Pantalla de Splash/Bienvenida (destino inicial) ---
        composable(route = NavigationRoutes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    // Navegar al login reemplazando el splash en la pila
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // --- Pantalla de Login ---
        composable(route = NavigationRoutes.LOGIN) {
            LoginScreen(
                userRepository = userRepository,
                onLoginSuccess = {
                    // Navegar al home y limpiar la pila de navegacion hasta login
                    navController.navigate(NavigationRoutes.HOME) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Navegar a la pantalla de registro
                    navController.navigate(NavigationRoutes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    // Navegar a la pantalla de olvido de contrasena
                    navController.navigate(NavigationRoutes.FORGOT_PASSWORD)
                }
            )
        }

        // --- Pantalla de Registro ---
        composable(route = NavigationRoutes.REGISTER) {
            RegisterScreen(
                userRepository = userRepository,
                onRegisterSuccess = {
                    // Volver al login despues del registro exitoso
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Navegar al login limpiando la pila
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Volver a la pantalla anterior
                    navController.popBackStack()
                }
            )
        }

        // --- Pantalla Principal (Home) ---
        composable(route = NavigationRoutes.HOME) {
            HomeScreen(
                userRepository = userRepository,
                onLogout = {
                    // Cerrar sesion y volver al login limpiando toda la pila
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- Pantalla de Olvido de Contrasena ---
        composable(route = NavigationRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                userRepository = userRepository,
                onCodeSent = { email ->
                    // Navegar a la pantalla de recuperacion con el email como argumento
                    navController.navigate(NavigationRoutes.recoverPasswordRoute(email))
                },
                onNavigateBack = {
                    // Volver a la pantalla anterior
                    navController.popBackStack()
                }
            )
        }

        // --- Pantalla de Recuperacion de Contrasena (con argumento email) ---
        composable(
            route = NavigationRoutes.RECOVER_PASSWORD,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            // Obtener el email del argumento de navegacion
            val email = backStackEntry.arguments?.getString("email") ?: ""

            RecoverPasswordScreen(
                email = email,
                userRepository = userRepository,
                onResetSuccess = {
                    // Volver al login despues de restablecer la contrasena
                    navController.navigate(NavigationRoutes.LOGIN) {
                        popUpTo(NavigationRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    // Volver a la pantalla anterior
                    navController.popBackStack()
                }
            )
        }
    }
}
