package co.uniquindio.unityevents.features.events

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/** Estado del formulario de creacion de evento. */
data class CreateEventUiState(
    val title: String = "",
    val description: String = "",
    val category: String = "Academico",
    val placeName: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startDate: Date? = null,
    val price: String = "0",
    val capacity: String = "",
    val imageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdEventId: String? = null
) {
    companion object {
        /** Categorias permitidas — se muestran como chips seleccionables. */
        val CATEGORIES = listOf("Academico", "Cultural", "Deportivo", "Social", "Tecnologia", "Otro")
    }
}

/**
 * ViewModel del formulario de creacion de evento. Lee el perfil del usuario actual para
 * guardar el snapshot de organizador, valida los campos y delega al repositorio la subida.
 */
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventUiState())
    val state: StateFlow<CreateEventUiState> = _state.asStateFlow()

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, errorMessage = null) }
    fun onDescriptionChange(v: String) = _state.update { it.copy(description = v, errorMessage = null) }
    fun onCategoryChange(v: String) = _state.update { it.copy(category = v) }
    fun onPlaceChange(v: String) = _state.update { it.copy(placeName = v, errorMessage = null) }
    fun onAddressChange(v: String) = _state.update { it.copy(address = v) }
    fun onStartDateChange(date: Date?) = _state.update { it.copy(startDate = date, errorMessage = null) }
    fun onPriceChange(v: String) = _state.update { it.copy(price = v.filter { c -> c.isDigit() }) }
    fun onCapacityChange(v: String) = _state.update { it.copy(capacity = v.filter { c -> c.isDigit() }) }
    fun onImagePicked(uri: Uri?) = _state.update { it.copy(imageUri = uri) }
    /** Actualiza las coordenadas del evento (tap en el mapa o "Usar mi ubicacion"). */
    fun onLocationPicked(lat: Double, lng: Double) =
        _state.update { it.copy(latitude = lat, longitude = lng, errorMessage = null) }
    fun onErrorConsumed() = _state.update { it.copy(errorMessage = null) }
    fun onCreatedConsumed() = _state.update { it.copy(createdEventId = null) }

    /** Valida campos obligatorios y dispara la creacion. */
    fun onSubmit() {
        val s = _state.value
        val title = s.title.trim()
        val description = s.description.trim()
        val placeName = s.placeName.trim()

        val validationError = when {
            title.isBlank() -> "El titulo es obligatorio."
            description.length < 20 -> "La descripcion debe tener al menos 20 caracteres."
            placeName.isBlank() -> "Indica el lugar del evento."
            s.latitude == null || s.longitude == null ->
                "Marca la ubicacion en el mapa (toca o usa tu ubicacion actual)."
            s.startDate == null -> "Selecciona la fecha y hora del evento."
            else -> null
        }
        if (validationError != null) {
            _state.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            val user = profileRepository.observeCurrentUser().firstOrNull()
            if (user == null) {
                _state.update { it.copy(isSubmitting = false, errorMessage = "Sesion expirada. Vuelve a entrar.") }
                return@launch
            }

            val event = Event(
                title = title,
                description = description,
                category = s.category,
                placeName = placeName,
                address = s.address.trim(),
                latitude = s.latitude,
                longitude = s.longitude,
                startDate = s.startDate,
                endDate = s.startDate, // simplificado en Fase B
                price = s.price.toLongOrNull() ?: 0L,
                capacity = s.capacity.toIntOrNull() ?: 0,
                organizerId = user.uid,
                organizerName = user.displayName,
                organizerPhotoUrl = user.photoUrl
            )

            eventsRepository.createEvent(event, s.imageUri)
                .onSuccess { eventId ->
                    _state.update {
                        CreateEventUiState(createdEventId = eventId)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isSubmitting = false, errorMessage = e.message ?: "No se pudo crear el evento.")
                    }
                }
        }
    }
}
