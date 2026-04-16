package co.uniquindio.unityevents.domain.model

import java.util.Date

/**
 * Reporte de contenido inapropiado (evento o comentario) generado por un usuario,
 * visible en el panel de moderacion.
 * Persistido en `/reports/{reportId}`.
 *
 * @property id id del documento.
 * @property reporterId uid de quien reporta.
 * @property targetType tipo de contenido reportado.
 * @property targetId id del evento o comentario reportado.
 * @property targetPreview snapshot del contenido para mostrar al moderador sin tener que navegar.
 * @property reason motivo seleccionado / escrito por el reportante.
 * @property status estado del reporte.
 * @property createdAt timestamp de creacion.
 */
data class Report(
    val id: String = "",
    val reporterId: String = "",
    val targetType: ReportTargetType = ReportTargetType.EVENT,
    val targetId: String = "",
    val targetPreview: String = "",
    val reason: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val createdAt: Date? = null
)

enum class ReportTargetType { EVENT, COMMENT;
    companion object {
        fun fromString(value: String?): ReportTargetType = values().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: EVENT
    }
}

enum class ReportStatus { PENDING, RESOLVED, DISMISSED;
    companion object {
        fun fromString(value: String?): ReportStatus = values().firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: PENDING
    }
}
