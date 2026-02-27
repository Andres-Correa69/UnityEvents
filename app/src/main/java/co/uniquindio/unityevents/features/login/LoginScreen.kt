package co.uniquindio.unityevents.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.uniquindio.unityevents.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.uniquindio.unityevents.core.component.PasswordTextField
import co.uniquindio.unityevents.core.component.PrimaryButton
import co.uniquindio.unityevents.domain.repository.UserRepository

/**
 * Pantalla de inicio de sesion de la aplicacion UnityEvents.
 *
 * Permite al usuario autenticarse con su correo electronico y contrasena.
 * Incluye enlaces para navegar a la pantalla de registro y la de
 * recuperacion de contrasena. Utiliza Snackbar para retroalimentacion.
 *
 * @param userRepository Repositorio de usuarios para autenticacion.
 * @param onLoginSuccess Callback invocado cuando el login es exitoso.
 * @param onNavigateToRegister Callback para navegar a la pantalla de registro.
 * @param onNavigateToForgotPassword Callback para navegar a la pantalla de olvido de contrasena.
 */
@Composable
fun LoginScreen(
    userRepository: UserRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    // Crear el ViewModel con el factory que inyecta el repositorio
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.provideFactory(userRepository)
    )

    // Observar el estado de la UI con soporte de ciclo de vida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado del host de Snackbar para mostrar mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Recolectar eventos de Snackbar del ViewModel
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Recolectar eventos de navegacion del ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { success ->
            if (success) onLoginSuccess()
        }
    }

    // Estructura principal de la pantalla con Scaffold de Material 3
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo de la aplicacion UnityEvents
            Image(
                painter = painterResource(id = R.drawable.logo_unity_events),
                contentDescription = "Logo UnityEvents",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Titulo de la pantalla
            Text(
                text = "Iniciar Sesion",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitulo de bienvenida
            Text(
                text = "Bienvenido de vuelta a UnityEvents",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de correo electronico
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Correo electronico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contrasena con visibilidad alternada
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Contrasena",
                isVisible = uiState.isPasswordVisible,
                onVisibilityToggle = viewModel::togglePasswordVisibility,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enlace para recuperar contrasena olvidada
            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "¿Olvidaste tu contrasena?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Boton principal de inicio de sesion
            PrimaryButton(
                text = "Iniciar Sesion",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace para navegar a la pantalla de registro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Registrate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
