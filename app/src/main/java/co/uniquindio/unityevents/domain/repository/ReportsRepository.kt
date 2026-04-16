package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.Report
import co.uniquindio.unityevents.domain.model.ReportStatus
import kotlinx.coroutines.flow.Flow

/** CRUD de reportes de contenido (`/reports`). */
interface ReportsRepository {

    /** Reportes pendientes (panel del moderador). */
    fun observePendingReports(): Flow<List<Report>>

    /** Todos los reportes filtrados por estado. */
    fun observeReportsByStatus(status: ReportStatus): Flow<List<Report>>

    /** Crea un reporte. */
    suspend fun submitReport(report: Report): Result<String>

    /** El moderador resuelve el reporte (lo marca como resuelto o descartado). */
    suspend fun updateStatus(reportId: String, status: ReportStatus): Result<Unit>
}
