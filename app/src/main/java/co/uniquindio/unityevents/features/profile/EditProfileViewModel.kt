package co.uniquindio.unityevents.features.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado del formulario "Editar perfil". */
data class EditProfileUiState(
    val displayName: String = "",
    val bio: String = "",
    val city: String = "",
    val photoUrl: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccess: Boolean = false
)

/**
 * ViewModel de Editar Perfil. Carga el perfil actual al iniciar; permite editar nombre,
 * bio y ciudad; y subir una nueva foto.
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditProfileUiState())
    val state: StateFlow<EditProfileUiState> = _state.asStateFlow()

    init { loadCurrent() }

    private fun loadCurrent() = viewModelScope.launch {
        val user = profileRepository.observeCurrentUser().firstOrNull()
        _state.update {
            it.copy(
                isLoading = false,
                displayName = user?.displayName.orEmpty(),
                bio = user?.bio.orEmpty(),
                city = user?.city.orEmpty(),
                photoUrl = user?.photoUrl
            )
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(displayName = v) }
    fun onBioChange(v: String) = _state.update { it.copy(bio = v) }
    fun onCityChange(v: String) = _state.update { it.copy(city = v) }

    /** Sube la nueva foto de perfil en background. */
    fun onPhotoPicked(uri: Uri) = viewModelScope.launch {
        profileRepository.updatePhoto(uri).fold(
            onSuccess = { url -> _state.update { it.copy(photoUrl = url) } },
            onFailure = { e -> _state.update { it.copy(errorMessage = e.message) } }
        )
    }

    fun onSave() {
        val s = _state.value
        if (s.displayName.isBlank()) {
            _state.update { it.copy(errorMessage = "El nombre no puede estar vacio.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            profileRepository.updateProfile(s.displayName.trim(), s.bio.trim(), s.city.trim()).fold(
                onSuccess = { _state.update { it.copy(isSaving = false, savedSuccess = true) } },
                onFailure = { e -> _state.update { it.copy(isSaving = false, errorMessage = e.message) } }
            )
        }
    }

    fun onErrorConsumed() = _state.update { it.copy(errorMessage = null) }
    fun onSavedConsumed() = _state.update { it.copy(savedSuccess = false) }
}
