package co.uniquindio.unityevents.features.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.uniquindio.unityevents.domain.model.Report
import co.uniquindio.unityevents.domain.model.ReportStatus
import co.uniquindio.unityevents.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Estado de la lista de reportes de contenido. */
data class ReportsListUiState(
    val reports: List<Report> = emptyList(),
    val isLoading: Boolean = true
)

/** ViewModel que gestiona los reportes de contenido inapropiado. */
@HiltViewModel
class ReportsListViewModel @Inject constructor(
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    val state: StateFlow<ReportsListUiState> = reportsRepository.observePendingReports()
        .map { ReportsListUiState(reports = it, isLoading = false) }
        .catch { emit(ReportsListUiState(isLoading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsListUiState(isLoading = true))

    fun resolve(reportId: String) = viewModelScope.launch {
        reportsRepository.updateStatus(reportId, ReportStatus.RESOLVED)
    }

    fun dismiss(reportId: String) = viewModelScope.launch {
        reportsRepository.updateStatus(reportId, ReportStatus.DISMISSED)
    }
}
