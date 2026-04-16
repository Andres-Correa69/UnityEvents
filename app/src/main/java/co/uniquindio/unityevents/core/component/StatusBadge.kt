package co.uniquindio.unityevents.core.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import co.uniquindio.unityevents.domain.model.EventStatus

/**
 * Chip compacto que indica visualmente el estado de moderacion de un evento.
 *
 * - PENDING → naranja "En revision" (el creador lo ve en su feed).
 * - REJECTED → rojo "Rechazado".
 * - APPROVED → verde "Aprobado" (solo se muestra si [showWhenApproved] es true).
 */
@Composable
fun StatusBadge(
    status: EventStatus,
    modifier: Modifier = Modifier,
    showWhenApproved: Boolean = false
) {
    if (status == EventStatus.APPROVED && !showWhenApproved) return

    val style = when (status) {
        EventStatus.PENDING -> BadgeStyle(
            background = Color(0xFFFFE1B3),
            foreground = Color(0xFF8A4B00),
            icon = Icons.Filled.HourglassTop,
            label = "En revision"
        )
        EventStatus.REJECTED -> BadgeStyle(
            background = MaterialTheme.colorScheme.errorContainer,
            foreground = MaterialTheme.colorScheme.onErrorContainer,
            icon = Icons.Filled.Cancel,
            label = "Rechazado"
        )
        EventStatus.APPROVED -> BadgeStyle(
            background = Color(0xFFCFE8CF),
            foreground = Color(0xFF1B5E20),
            icon = Icons.Filled.CheckCircle,
            label = "Aprobado"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(style.background)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.foreground,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = style.label,
            style = MaterialTheme.typography.labelSmall,
            color = style.foreground
        )
    }
}

/** Pequena data class privada para agrupar los 4 valores visuales del badge. */
private data class BadgeStyle(
    val background: Color,
    val foreground: Color,
    val icon: ImageVector,
    val label: String
)
