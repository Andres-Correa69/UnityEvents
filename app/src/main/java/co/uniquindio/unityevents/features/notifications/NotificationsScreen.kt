package co.uniquindio.unityevents.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.uniquindio.unityevents.core.component.EmptyState
import co.uniquindio.unityevents.core.component.LoadingBox
import co.uniquindio.unityevents.core.utils.Formatters
import co.uniquindio.unityevents.domain.model.Notification
import co.uniquindio.unityevents.domain.model.NotificationType

/** Lista de notificaciones del usuario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                actions = {
                    if (state.notifications.any { !it.read }) {
                        IconButton(onClick = viewModel::onMarkAllRead) {
                            Icon(Icons.Filled.DoneAll, contentDescription = "Marcar todas como leidas")
                        }
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
            state.notifications.isEmpty() -> EmptyState(
                icon = Icons.Filled.Notifications,
                title = "Sin notificaciones",
                message = "Cuando pase algo importante te avisaremos aqui.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding() + 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state.notifications, key = { it.id }) { notif ->
                    NotificationRow(
                        notification = notif,
                        onClick = { viewModel.onNotificationClick(notif.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    val (icon, tint) = iconFor(notification.type)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (!notification.read) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                notification.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal
                )
            )
            Text(
                notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Text(
                Formatters.formatShortDate(notification.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!notification.read) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/** Icono segun el tipo de notificacion. */
@Composable
private fun iconFor(type: NotificationType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    NotificationType.EVENT_APPROVED -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
    NotificationType.EVENT_REJECTED -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
    NotificationType.EVENT_REMINDER -> Icons.Filled.Event to MaterialTheme.colorScheme.primary
    NotificationType.NEW_COMMENT -> Icons.Filled.Comment to MaterialTheme.colorScheme.primary
    NotificationType.TICKET_PURCHASED -> Icons.Filled.ConfirmationNumber to MaterialTheme.colorScheme.primary
    NotificationType.INFO -> Icons.Filled.Notifications to MaterialTheme.colorScheme.primary
}
