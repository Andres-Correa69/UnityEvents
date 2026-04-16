package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.Report
import co.uniquindio.unityevents.domain.model.ReportStatus
import co.uniquindio.unityevents.domain.model.ReportTargetType
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/** DTO de `/reports/{reportId}`. */
data class ReportDto(
    var id: String = "",
    var reporterId: String = "",
    var targetType: String = ReportTargetType.EVENT.name,
    var targetId: String = "",
    var targetPreview: String = "",
    var reason: String = "",
    var status: String = ReportStatus.PENDING.name,
    @ServerTimestamp var createdAt: Date? = null
) {
    fun toDomain(): Report = Report(
        id = id,
        reporterId = reporterId,
        targetType = ReportTargetType.fromString(targetType),
        targetId = targetId,
        targetPreview = targetPreview,
        reason = reason,
        status = ReportStatus.fromString(status),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(r: Report): ReportDto = ReportDto(
            id = r.id,
            reporterId = r.reporterId,
            targetType = r.targetType.name,
            targetId = r.targetId,
            targetPreview = r.targetPreview,
            reason = r.reason,
            status = r.status.name
        )
    }
}
