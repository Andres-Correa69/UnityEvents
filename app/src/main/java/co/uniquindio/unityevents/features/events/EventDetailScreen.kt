package co.uniquindio.unityevents.features.events

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.uniquindio.unityevents.core.component.CategoryChip
import co.uniquindio.unityevents.core.component.EmptyState
import co.uniquindio.unityevents.core.component.LoadingBox
import co.uniquindio.unityevents.core.component.StarRating
import co.uniquindio.unityevents.core.component.UserAvatar
import co.uniquindio.unityevents.core.utils.Formatters
import co.uniquindio.unityevents.domain.model.Comment
import co.uniquindio.unityevents.domain.model.Event

/**
 * Detalle de un evento: imagen, meta-informacion, descripcion, comentarios y boton para
 * obtener el ticket.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    onBack: () -> Unit,
    onTicketPurchased: (ticketId: String) -> Unit,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Al completarse la compra, navegamos al ticket digital.
    LaunchedEffect(state.purchasedTicketId) {
        state.purchasedTicketId?.let {
            viewModel.onPurchaseConsumed()
            onTicketPurchased(it)
        }
    }
    // Muestra errores como snackbar.
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorConsumed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evento") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atras")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingBox(modifier = Modifier.padding(innerPadding))
            state.event == null -> EmptyState(
                icon = Icons.Filled.Flag,
                title = "Evento no encontrado",
                message = "Pudo haber sido eliminado por el organizador.",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
            else -> EventDetailContent(
                state = state,
                event = state.event!!,
                modifier = Modifier.padding(innerPadding),
                onBuyClick = viewModel::onBuyTicketClick,
                onCommentTextChange = viewModel::onCommentTextChange,
                onCommentRatingChange = viewModel::onCommentRatingChange,
                onSubmitComment = viewModel::onSubmitComment
            )
        }
    }
}

@Composable
private fun EventDetailContent(
    state: EventDetailUiState,
    event: Event,
    onBuyClick: () -> Unit,
    onCommentTextChange: (String) -> Unit,
    onCommentRatingChange: (Int) -> Unit,
    onSubmitComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero: imagen o placeholder de color de marca.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (event.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
            if (event.category.isNotBlank()) {
                CategoryChip(
                    text = event.category,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Info principal.
        Column(Modifier.padding(20.dp)) {
            Text(
                event.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(12.dp))

            InfoRow(icon = Icons.Filled.CalendarToday, text = Formatters.formatEventDate(event.startDate))
            InfoRow(icon = Icons.Filled.LocationOn, text = event.placeName.ifBlank { "Sin lugar" })
            InfoRow(icon = Icons.Filled.Paid, text = Formatters.formatPrice(event.price))
            InfoRow(icon = Icons.Filled.Groups, text = "${event.attendeesCount} asistentes")

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Text("Acerca del evento", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                event.description.ifBlank { "Sin descripcion." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            // Organizador.
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    photoUrl = event.organizerPhotoUrl,
                    displayName = event.organizerName,
                    size = 40
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("Organizador", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        event.organizerName.ifBlank { "Anonimo" },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Boton primario: obtener ticket.
            Button(
                onClick = onBuyClick,
                enabled = !state.isPurchasing && state.currentUser != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isPurchasing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(Icons.Filled.ConfirmationNumber, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Obtener ticket", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Seccion de comentarios.
            Text("Comentarios (${state.comments.size})", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            // Formulario para dejar un comentario.
            if (state.currentUser != null) {
                OutlinedTextField(
                    value = state.commentText,
                    onValueChange = onCommentTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe tu comentario...") },
                    minLines = 2,
                    shape = MaterialTheme.shapes.large
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StarRating(
                        rating = state.commentRating,
                        onRatingChanged = onCommentRatingChange
                    )
                    Spacer(Modifier.size(12.dp))
                    Text(
                        "Calificacion",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = onSubmitComment,
                        enabled = state.commentText.isNotBlank() && !state.isSubmittingComment
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Publicar",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Lista de comentarios.
            if (state.comments.isEmpty()) {
                Text(
                    "Aun no hay comentarios. Se el primero en opinar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.comments.forEach { CommentItem(it) }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        UserAvatar(
            photoUrl = comment.authorPhotoUrl,
            displayName = comment.authorName,
            size = 36
        )
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.authorName.ifBlank { "Anonimo" },
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.size(8.dp))
                if (comment.rating > 0) {
                    StarRating(rating = comment.rating, size = 14)
                }
            }
            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
            Text(
                Formatters.formatShortDate(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
