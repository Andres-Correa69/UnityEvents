package co.uniquindio.unityevents.features.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.core.component.LoadingBox
import co.uniquindio.unityevents.core.component.UserAvatar
import co.uniquindio.unityevents.domain.model.UserRole

/**
 * Pantalla de perfil del usuario. Muestra avatar, datos, progreso de nivel, estadisticas y
 * accesos rapidos (editar perfil, niveles, ajustes, panel moderador si aplica).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLevels: () -> Unit,
    onModeratorDashboard: () -> Unit,
    onQrScanner: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Ajustes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingBox(Modifier.padding(innerPadding))
            state.user == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text("Sesion expirada.") }
            else -> ProfileContent(
                state = state,
                modifier = Modifier.padding(innerPadding),
                onEditProfile = onEditProfile,
                onLevels = onLevels,
                onModeratorDashboard = onModeratorDashboard,
                onQrScanner = onQrScanner
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditProfile: () -> Unit,
    onLevels: () -> Unit,
    onModeratorDashboard: () -> Unit,
    onQrScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user = state.user!!
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Avatar + nombre + rol.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(photoUrl = user.photoUrl, displayName = user.displayName, size = 96)
            Spacer(Modifier.height(12.dp))
            Text(
                user.displayName.ifBlank { "Anonimo" },
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (user.role == UserRole.MODERATOR || user.role == UserRole.ADMIN) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AdminPanelSettings, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(user.role.name, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Bio y ciudad (si hay).
        if (user.bio.isNotBlank() || user.city.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (user.city.isNotBlank()) {
                        Text(user.city, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                    }
                    if (user.bio.isNotBlank()) {
                        Text(user.bio, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Tarjeta de nivel y puntos.
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onLevels),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Nivel ${user.level}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.weight(1f))
                    Text("${user.points} pts",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { state.levelProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Asiste y crea eventos para ganar puntos y subir de nivel.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Estadisticas rapidas.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Eventos creados",
                value = state.myEvents.size.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Aprobados",
                value = state.myEvents.count {
                    it.status == co.uniquindio.unityevents.domain.model.EventStatus.APPROVED
                }.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Acciones.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("Editar perfil") },
                    leadingContent = { Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable(onClick = onEditProfile)
                )
                ListItem(
                    headlineContent = { Text("Mi reputacion y niveles") },
                    leadingContent = { Icon(Icons.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable(onClick = onLevels)
                )
                if (user.role == UserRole.MODERATOR || user.role == UserRole.ADMIN) {
                    ListItem(
                        headlineContent = { Text("Panel de moderacion") },
                        leadingContent = { Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable(onClick = onModeratorDashboard)
                    )
                    ListItem(
                        headlineContent = { Text("Escanear QR de tickets") },
                        leadingContent = { Icon(Icons.Filled.QrCodeScanner, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable(onClick = onQrScanner)
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ))
            Text(title, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

