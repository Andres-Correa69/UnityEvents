package co.uniquindio.unityevents.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.repository.NotificationsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel minimo consumido por el bottom nav para mostrar el badge con el numero de
 * notificaciones sin leer. Se suscribe en vivo a Firestore.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BottomBarBadgeViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository
) : ViewModel() {

    val unread: StateFlow<Int> = profileRepository.observeCurrentUser()
        .flatMapLatest { user ->
            if (user == null) flowOf(0)
            else notificationsRepository.observeUnreadCount(user.uid)
        }
        // Si Firestore tira PERMISSION_DENIED u otro error, emitimos 0 en vez de crashear.
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
