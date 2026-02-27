package co.uniquindio.unityevents.features.recoverpassword

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
import androidx.compose.material.icons.filled.LockReset
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
import co.uniquindio.unityevents.core.component.PasswordTextField
import co.uniquindio.unityevents.core.component.PrimaryButton
import co.uniquindio.unityevents.domain.repository.UserRepository

/**
 * Pantalla de recuperacion de contrasena de UnityEvents.
 *
 * Permite al usuario ingresar el codigo de recuperacion recibido
 * y establecer una nueva contrasena. Valida el codigo y las
 * contrasenas antes de realizar el restablecimiento.
 *
 * @param email Correo electronico del usuario que solicita la recuperacion.
 * @param userRepository Repositorio de usuarios para validar y restablecer la contrasena.
 * @param onResetSuccess Callback invocado cuando la contrasena se restablece exitosamente.
 * @param onNavigateBack Callback para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoverPasswordScreen(
    email: String,
    userRepository: UserRepository,
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Crear el ViewModel con el factory que inyecta el email y repositorio
    val viewModel: RecoverPasswordViewModel = viewModel(
        factory = RecoverPasswordViewModel.provideFactory(email, userRepository)
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
            if (success) onResetSuccess()
        }
    }

    // Estructura principal con Scaffold, TopAppBar y SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Nueva Contrasena")
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
            Spacer(modifier = Modifier.height(32.dp))

            // Icono decorativo de restablecimiento
            Icon(
                imageVector = Icons.Filled.LockReset,
                contentDescription = "Restablecer contrasena",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Titulo de la pantalla
            Text(
                text = "Restablecer Contrasena",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripcion informativa del proceso
            Text(
                text = "Ingresa el codigo de recuperacion que recibiste y establece tu nueva contrasena.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo para el codigo de recuperacion
            OutlinedTextField(
                value = uiState.code,
                onValueChange = viewModel::onCodeChanged,
                label = { Text("Codigo de recuperacion") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo para la nueva contrasena
            PasswordTextField(
                value = uiState.newPassword,
                onValueChange = viewModel::onNewPasswordChanged,
                label = "Nueva contrasena",
                isVisible = uiState.isPasswordVisible,
                onVisibilityToggle = viewModel::togglePasswordVisibility,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo para confirmar la nueva contrasena
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                label = "Confirmar nueva contrasena",
                isVisible = uiState.isConfirmPasswordVisible,
                onVisibilityToggle = viewModel::toggleConfirmPasswordVisibility,
                modifier = Modifier.fillMaxWidth(),
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Boton para restablecer la contrasena
            PrimaryButton(
                text = "Restablecer Contrasena",
                onClick = viewModel::resetPassword,
                modifier = Modifier.fillMaxWidth(),
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
