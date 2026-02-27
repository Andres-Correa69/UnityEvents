package co.uniquindio.unityevents.features.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.uniquindio.unityevents.core.component.PrimaryButton
import co.uniquindio.unityevents.domain.repository.UserRepository

/**
 * Pantalla de olvido de contrasena de UnityEvents.
 *
 * Permite al usuario ingresar su correo electronico para recibir
 * un codigo de recuperacion. Una vez enviado el codigo, navega
 * a la pantalla de recuperacion de contrasena.
 *
 * @param userRepository Repositorio de usuarios para verificar el email.
 * @param onCodeSent Callback invocado cuando el codigo es enviado, recibe el email como parametro.
 * @param onNavigateBack Callback para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    userRepository: UserRepository,
    onCodeSent: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Crear el ViewModel con el factory que inyecta el repositorio
    val viewModel: ForgotPasswordViewModel = viewModel(
        factory = ForgotPasswordViewModel.provideFactory(userRepository)
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

    // Recolectar eventos de navegacion del ViewModel (email para la siguiente pantalla)
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { email ->
            onCodeSent(email)
        }
    }

    // Estructura principal con Scaffold, TopAppBar y SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Recuperar Contrasena")
                },
                navigationIcon = {
                    // Boton de retroceso en la barra superior
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
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
            Spacer(modifier = Modifier.height(48.dp))

            // Icono decorativo de email/recuperacion
            Icon(
                imageVector = Icons.Filled.MarkEmailRead,
                contentDescription = "Recuperar contrasena",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Titulo de la pantalla
            Text(
                text = "¿Olvidaste tu contrasena?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripcion informativa del proceso
            Text(
                text = "Ingresa tu correo electronico y te enviaremos un codigo de recuperacion para restablecer tu contrasena.",
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
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Boton para enviar el codigo de recuperacion
            PrimaryButton(
                text = "Enviar Codigo",
                onClick = viewModel::sendRecoveryCode,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
