package co.uniquindio.unityevents.features.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.uniquindio.unityevents.R
import co.uniquindio.unityevents.domain.repository.UserRepository

/**
 * Pantalla principal (Home) de la aplicacion UnityEvents.
 *
 * Muestra un mensaje de bienvenida personalizado con el nombre del usuario,
 * el logo de la aplicacion y una descripcion de la app.
 * Incluye funcionalidad de cierre de sesion en la barra superior.
 *
 * @param userRepository Repositorio de usuarios para obtener datos del usuario actual.
 * @param onLogout Callback invocado cuando el usuario cierra sesion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userRepository: UserRepository,
    onLogout: () -> Unit
) {
    // Crear el ViewModel con el factory que inyecta el repositorio
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(userRepository)
    )

    // Observar el estado de la UI con soporte de ciclo de vida
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado del host de Snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Obtener el nombre del usuario para la bienvenida
    val userName = uiState.currentUser?.name ?: "Usuario"

    // Estructura principal con Scaffold, TopAppBar y SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "UnityEvents")
                },
                actions = {
                    // Boton de cerrar sesion en la barra superior
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesion",
                            tint = MaterialTheme.colorScheme.onSurface
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

            // Logo de la aplicacion UnityEvents
            Image(
                painter = painterResource(id = R.drawable.logo_unity_events),
                contentDescription = "Logo UnityEvents",
                modifier = Modifier.size(180.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de bienvenida personalizado con el nombre del usuario
            Text(
                text = "Bienvenido, $userName",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card con la descripcion de la aplicacion
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Acerca de UnityEvents",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "UnityEvents es tu plataforma para descubrir, crear y gestionar " +
                                "eventos de manera sencilla. Conecta con tu comunidad, organiza " +
                                "encuentros memorables y no te pierdas ninguna actividad importante. " +
                                "Desde conferencias y talleres hasta fiestas y reuniones sociales, " +
                                "UnityEvents te mantiene al tanto de todo lo que sucede a tu alrededor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card con las caracteristicas principales de la aplicacion
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Caracteristicas principales",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Lista de caracteristicas de la app
                    val features = listOf(
                        "Explora eventos cerca de ti",
                        "Crea y comparte tus propios eventos",
                        "Gestiona asistentes facilmente",
                        "Recibe notificaciones de eventos importantes",
                        "Conecta con personas afines"
                    )
                    features.forEach { feature ->
                        Text(
                            text = "• $feature",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
