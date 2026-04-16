package co.uniquindio.unityevents.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Notification
import co.uniquindio.unityevents.domain.repository.NotificationsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la pantalla de notificaciones. */
data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * ViewModel de la pantalla de notificaciones. Observa en vivo la subcoleccion del usuario
 * autenticado y expone acciones para marcar como leidas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val notificationsRepository: NotificationsRepository
) : ViewModel() {

    val state: StateFlow<NotificationsUiState> = profileRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else notificationsRepository.observeNotifications(user.uid)
        }
        .map { list -> NotificationsUiState(notifications = list, isLoading = false) }
        .catch { e -> emit(NotificationsUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState(isLoading = true))

    fun onNotificationClick(notificationId: String) = viewModelScope.launch {
        val user = profileRepository.observeCurrentUser().firstOrNull() ?: return@launch
        notificationsRepository.markAsRead(user.uid, notificationId)
    }

    fun onMarkAllRead() = viewModelScope.launch {
        val user = profileRepository.observeCurrentUser().firstOrNull() ?: return@launch
        notificationsRepository.markAllAsRead(user.uid)
    }
}
