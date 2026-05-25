package co.uniquindio.unityevents.features.events

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.core.service.ContentModerationService
import co.uniquindio.unityevents.domain.model.Event
import co.uniquindio.unityevents.domain.model.ModerationResult
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

/**
 * Estado del formulario de creacion / edicion de evento.
 *
 * @property editingEventId Si != null, el formulario esta editando un evento existente
 *           y al guardar invocara updateEvent en vez de createEvent.
 * @property existingImageUrl Imagen actual del evento cuando se esta editando — se muestra
 *           como preview hasta que el usuario elija una nueva.
 * @property savedEventId Id del evento creado o actualizado, usado para navegar al detalle.
 */
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
    val existingImageUrl: String? = null,
    val editingEventId: String? = null,
    val isLoadingExisting: Boolean = false,
    val isSubmitting: Boolean = false,
    val isModerating: Boolean = false,
    val moderationBlock: ModerationResult? = null,
    /**
     * Mensaje informativo del resultado de la moderacion IA (snackbar). Distinto a
     * [moderationBlock] que es modal y detiene el flujo. Aqui solo mostramos:
     *   - "Contenido verificado por IA" (cuando aprueba) → SUCCESS
     *   - "La IA no pudo verificar..." (fail-open por error) → WARNING
     * El flujo continua igual; este mensaje es solo para que el usuario sepa lo que paso.
     */
    val moderationStatusMessage: String? = null,
    val errorMessage: String? = null,
    val savedEventId: String? = null
) {
    /** True cuando estamos editando un evento ya creado. */
    val isEditMode: Boolean get() = editingEventId != null

    companion object {
        /** Categorias permitidas — se muestran como chips seleccionables. */
        val CATEGORIES = listOf("Academico", "Cultural", "Deportivo", "Social", "Tecnologia", "Otro")
    }
}

/**
 * ViewModel del formulario de creacion / edicion de evento.
 *
 * Si la ruta de navegacion incluye el argumento `eventId`, entra en modo edicion: precarga
 * el evento existente y al guardar invoca updateEvent (preservando status y attendeesCount).
 * Si no, funciona en modo creacion (status PENDING, espera moderacion).
 */
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val eventsRepository: EventsRepository,
    private val profileRepository: ProfileRepository,
    private val contentModerationService: ContentModerationService
) : ViewModel() {

    /** Id del evento a editar; null cuando estamos creando uno nuevo. */
    private val editingEventId: String? = savedState.get<String>("eventId")

    private val _state = MutableStateFlow(
        CreateEventUiState(
            editingEventId = editingEventId,
            isLoadingExisting = editingEventId != null
        )
    )
    val state: StateFlow<CreateEventUiState> = _state.asStateFlow()

    init {
        // En modo edicion, cargamos el evento existente y poblamos el formulario.
        if (editingEventId != null) loadExisting(editingEventId)
    }

    /** Lee el evento una sola vez (firstOrNull) y rellena los campos del formulario. */
    private fun loadExisting(eventId: String) {
        viewModelScope.launch {
            val event = eventsRepository.observeEvent(eventId).firstOrNull()
            if (event == null) {
                _state.update {
                    it.copy(
                        isLoadingExisting = false,
                        errorMessage = "No se pudo cargar el evento para editar."
                    )
                }
                return@launch
            }
            _state.update {
                it.copy(
                    title = event.title,
                    description = event.description,
                    category = event.category.ifBlank { "Academico" },
                    placeName = event.placeName,
                    address = event.address,
                    latitude = event.latitude,
                    longitude = event.longitude,
                    startDate = event.startDate,
                    price = event.price.toString(),
                    capacity = if (event.capacity == 0) "" else event.capacity.toString(),
                    existingImageUrl = event.imageUrl.ifBlank { null },
                    isLoadingExisting = false
                )
            }
        }
    }

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
    fun onSavedConsumed() = _state.update { it.copy(savedEventId = null) }

    /**
     * Cierra el dialogo de bloqueo de la IA. El usuario edita el formulario y al tocar
     * "Publicar" otra vez se vuelve a ejecutar el analisis con el contenido actualizado.
     */
    fun onModerationBlockDismissed() = _state.update { it.copy(moderationBlock = null) }

    /** Marca el snackbar de estado de la IA como ya mostrado. */
    fun onModerationStatusConsumed() = _state.update { it.copy(moderationStatusMessage = null) }

    /** Valida campos obligatorios y dispara crear o actualizar segun el modo. */
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
            _state.update { it.copy(isSubmitting = true, isModerating = true, errorMessage = null) }

            // Filtro 1 (automatico): IA analiza titulo + descripcion + imagen. Si detecta
            // contenido prohibido, mostramos dialogo y NO creamos. Si pasa o falla (fail-open),
            // continuamos al flujo normal donde el moderador humano es el filtro final.
            val moderation = contentModerationService.analyze(
                title = title,
                description = description,
                imageUri = s.imageUri
            )
            if (!moderation.approved) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        isModerating = false,
                        moderationBlock = moderation
                    )
                }
                return@launch
            }
            // Mensaje informativo distinto segun si la IA REALMENTE aprobo o si fue fail-open.
            // Asi el usuario / profesor sabe en que estado quedo el contenido.
            val statusMessage = if (moderation.isFailOpen) {
                "La IA no pudo verificar el contenido. Sera revisado por un moderador."
            } else {
                "Contenido verificado por la IA."
            }
            _state.update {
                it.copy(isModerating = false, moderationStatusMessage = statusMessage)
            }

            val user = profileRepository.observeCurrentUser().firstOrNull()
            if (user == null) {
                _state.update { it.copy(isSubmitting = false, errorMessage = "Sesion expirada. Vuelve a entrar.") }
                return@launch
            }

            val baseEvent = Event(
                id = s.editingEventId.orEmpty(),
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
                imageUrl = s.existingImageUrl.orEmpty(),
                organizerId = user.uid,
                organizerName = user.displayName,
                organizerPhotoUrl = user.photoUrl
            )

            val result = if (s.isEditMode) {
                // Modo edicion: updateEvent devuelve Unit, mapeamos al id existente.
                eventsRepository.updateEvent(baseEvent, s.imageUri).map { s.editingEventId!! }
            } else {
                eventsRepository.createEvent(baseEvent, s.imageUri)
            }

            result
                .onSuccess { eventId ->
                    _state.update { it.copy(isSubmitting = false, savedEventId = eventId) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = e.message ?: if (s.isEditMode)
                                "No se pudo actualizar el evento."
                            else "No se pudo crear el evento."
                        )
                    }
                }
        }
    }
}
