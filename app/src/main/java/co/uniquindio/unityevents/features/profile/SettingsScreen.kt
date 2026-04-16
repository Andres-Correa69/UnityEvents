package co.uniquindio.unityevents.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import co.uniquindio.unityevents.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Pantalla de Ajustes. Muestra opciones clasicas (notificaciones, privacidad, terminos) —
 * en Fase B son principalmente placeholders — y el boton de cerrar sesion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {

            ListItem(
                headlineContent = { Text("Notificaciones") },
                leadingContent = { Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text("Gestiona el tipo de alertas que recibes") },
                modifier = Modifier.clickable { /* placeholder */ }
            )
            ListItem(
                headlineContent = { Text("Privacidad") },
                leadingContent = { Icon(Icons.Filled.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text("Quien puede ver tu perfil y actividad") },
                modifier = Modifier.clickable { /* placeholder */ }
            )
            ListItem(
                headlineContent = { Text("Seguridad") },
                leadingContent = { Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text("Cambiar contrasena, sesiones") },
                modifier = Modifier.clickable { /* placeholder */ }
            )
            ListItem(
                headlineContent = { Text("Acerca de UnityEvents") },
                leadingContent = { Icon(Icons.Filled.Info, null, tint = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text("Version 1.0.0") },
                modifier = Modifier.clickable { /* placeholder */ }
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Cerrar sesion") },
                leadingContent = {
                    Icon(Icons.AutoMirrored.Filled.Logout, null,
                        tint = MaterialTheme.colorScheme.error)
                },
                colors = ListItemDefaults.colors(
                    headlineColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.clickable {
                    viewModel.signOut()
                    onSignedOut()
                }
            )
        }
    }
}

/** ViewModel minimo — solo envuelve sign out. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun signOut() = authRepository.signOut()
}
