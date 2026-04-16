package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.ReportDto
import co.uniquindio.unityevents.domain.model.Report
import co.uniquindio.unityevents.domain.model.ReportStatus
import co.uniquindio.unityevents.domain.repository.ReportsRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Impl de reportes `/reports`. */
@Singleton
class ReportsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportsRepository {

    private val collection get() = firestore.collection(REPORTS)

    override fun observePendingReports(): Flow<List<Report>> =
        observeByStatus(ReportStatus.PENDING)

    override fun observeReportsByStatus(status: ReportStatus): Flow<List<Report>> =
        observeByStatus(status)

    private fun observeByStatus(status: ReportStatus): Flow<List<Report>> = callbackFlow {
        // Ordenamiento client-side para no requerir indice compuesto.
        val registration = collection
            .whereEqualTo("status", status.name)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents.orEmpty()
                    .mapNotNull { doc -> doc.toObject(ReportDto::class.java)?.copy(id = doc.id)?.toDomain() }
                    .sortedByDescending { it.createdAt }
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override suspend fun submitReport(report: Report): Result<String> = runCatching {
        val ref = collection.document()
        ref.set(ReportDto.fromDomain(report.copy(id = ref.id))).await()
        ref.id
    }

    override suspend fun updateStatus(reportId: String, status: ReportStatus): Result<Unit> =
        runCatching {
            collection.document(reportId)
                .set(mapOf("status" to status.name), SetOptions.merge())
                .await()
        }

    private companion object {
        const val REPORTS = "reports"
    }
}
