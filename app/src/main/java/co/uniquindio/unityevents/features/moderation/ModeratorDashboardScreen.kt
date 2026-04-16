package co.uniquindio.unityevents.features.moderation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.domain.model.EventStatus

/**
 * Panel del moderador: tarjetas con contadores de eventos por estado + reportes.
 * Cada tarjeta lleva a la lista correspondiente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorDashboardScreen(
    onBack: () -> Unit,
    onOpenList: (filter: String) -> Unit,
    onOpenReports: () -> Unit,
    viewModel: ModeratorDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de moderacion") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(8.dp))
                Text("Revision de contenido",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            // Pendientes (destacado).
            DashboardCard(
                icon = Icons.Filled.HourglassTop,
                title = "Eventos pendientes",
                count = state.pendingEvents,
                subtitle = "Esperando aprobacion",
                accent = MaterialTheme.colorScheme.primary,
                onClick = { onOpenList(EventStatus.PENDING.name) }
            )
            DashboardCard(
                icon = Icons.Filled.CheckCircle,
                title = "Eventos aprobados",
                count = state.approvedEvents,
                subtitle = "Visibles en el feed",
                accent = Color(0xFF2E7D32),
                onClick = { onOpenList(EventStatus.APPROVED.name) }
            )
            DashboardCard(
                icon = Icons.Filled.Cancel,
                title = "Eventos rechazados",
                count = state.rejectedEvents,
                subtitle = "No visibles",
                accent = MaterialTheme.colorScheme.error,
                onClick = { onOpenList(EventStatus.REJECTED.name) }
            )
            DashboardCard(
                icon = Icons.Filled.Flag,
                title = "Reportes de contenido",
                count = state.pendingReports,
                subtitle = "Validacion de contenido inapropiado",
                accent = MaterialTheme.colorScheme.tertiary,
                onClick = onOpenReports
            )
        }
    }
}

@Composable
private fun DashboardCard(
    icon: ImageVector,
    title: String,
    count: Int,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(36.dp))
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "$count",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            )
        }
    }
}
