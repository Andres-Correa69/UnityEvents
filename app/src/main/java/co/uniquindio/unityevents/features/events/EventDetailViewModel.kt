package co.uniquindio.unityevents.features.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Comment
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.CommentsRepository
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import co.uniquindio.unityevents.domain.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado UI de la pantalla de detalle de evento. */
data class EventDetailUiState(
    val event: Event? = null,
    val comments: List<Comment> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    // Estado del formulario de comentario.
    val commentText: String = "",
    val commentRating: Int = 0,
    val isSubmittingComment: Boolean = false,
    // Estado de la compra de ticket.
    val isPurchasing: Boolean = false,
    val purchasedTicketId: String? = null
)

/**
 * ViewModel del detalle de evento. Observa en vivo el evento, sus comentarios y el usuario,
 * y orquesta compra de ticket + envio de comentarios.
 */
@HiltViewModel
class EventDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val eventsRepository: EventsRepository,
    private val commentsRepository: CommentsRepository,
    private val ticketsRepository: TicketsRepository,
    profileRepository: ProfileRepository
) : ViewModel() {

    /** Id del evento recibido via argumentos de navegacion. */
    private val eventId: String = checkNotNull(savedState.get<String>("eventId")) {
        "EventDetailScreen requiere argumento 'eventId'."
    }

    private val _formState = MutableStateFlow(EventDetailUiState(isLoading = true))

    val state: StateFlow<EventDetailUiState> = combine(
        eventsRepository.observeEvent(eventId),
        commentsRepository.observeComments(eventId),
        profileRepository.observeCurrentUser(),
        _formState
    ) { event, comments, user, form ->
        form.copy(
            event = event,
            comments = comments,
            currentUser = user,
            isLoading = false
        )
    }
        .catch { e ->
            emit(_formState.value.copy(isLoading = false, errorMessage = e.message))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventDetailUiState(isLoading = true))

    // --- Acciones del formulario de comentario ---------------------------------

    fun onCommentTextChange(value: String) {
        _formState.update { it.copy(commentText = value) }
    }

    fun onCommentRatingChange(value: Int) {
        _formState.update { it.copy(commentRating = value) }
    }

    fun onSubmitComment() {
        val snap = state.value
        val user = snap.currentUser ?: return
        val text = snap.commentText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _formState.update { it.copy(isSubmittingComment = true) }
            val comment = Comment(
                eventId = eventId,
                authorId = user.uid,
                authorName = user.displayName.ifBlank { "Anonimo" },
                authorPhotoUrl = user.photoUrl,
                text = text,
                rating = snap.commentRating
            )
            commentsRepository.addComment(comment)
                .onSuccess {
                    _formState.update {
                        it.copy(
                            commentText = "",
                            commentRating = 0,
                            isSubmittingComment = false
                        )
                    }
                }
                .onFailure { e ->
                    _formState.update {
                        it.copy(
                            isSubmittingComment = false,
                            errorMessage = e.message ?: "No se pudo publicar el comentario."
                        )
                    }
                }
        }
    }

    // --- Compra de ticket ------------------------------------------------------

    fun onBuyTicketClick() {
        val snap = state.value
        val event = snap.event ?: return
        val user = snap.currentUser ?: return
        if (snap.isPurchasing) return

        viewModelScope.launch {
            _formState.update { it.copy(isPurchasing = true) }
            ticketsRepository.purchaseTicket(event, user.uid, user.displayName)
                .onSuccess { ticketId ->
                    _formState.update {
                        it.copy(isPurchasing = false, purchasedTicketId = ticketId)
                    }
                }
                .onFailure { e ->
                    _formState.update {
                        it.copy(isPurchasing = false, errorMessage = e.message ?: "No se pudo obtener el ticket.")
                    }
                }
        }
    }

    fun onPurchaseConsumed() {
        _formState.update { it.copy(purchasedTicketId = null) }
    }

    fun onErrorConsumed() {
        _formState.update { it.copy(errorMessage = null) }
    }
}
