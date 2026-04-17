package co.uniquindio.unityevents.core.service

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio centralizado del sistema de reputacion y niveles de UnityEvents.
 *
 * Todas las acciones que otorgan puntos pasan por aqui para tener una sola fuente
 * de verdad de las reglas. Usa `FieldValue.increment` para evitar race conditions
 * cuando varios clientes actualizan el mismo usuario.
 *
 * Reglas (alineadas con LevelsScreen):
 *  - [Reward.EVENT_APPROVED]   = +50 pts para el organizador
 *  - [Reward.COMMENT_ADDED]    = +10 pts para el autor del comentario
 *  - [Reward.COMMENT_5_STARS]  = +20 pts adicional al organizador cuando reciben 5 estrellas
 *  - [Reward.TICKET_SCANNED]   = +30 pts al asistente cuando su ticket es validado
 *
 * Tras actualizar los puntos re-calcula el nivel: `level = (points / 500) + 1`, con maximo 5.
 */
@Singleton
class ReputationService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /** Catalogo de recompensas como constantes nombradas. */
    object Reward {
        const val EVENT_APPROVED = 50
        const val COMMENT_ADDED = 10
        const val COMMENT_5_STARS = 20
        const val TICKET_SCANNED = 30
    }

    /**
     * Suma [points] al usuario [userId] y recalcula su nivel.
     *
     * Si algo falla, el error se traga silenciosamente: la reputacion es una feature
     * "best-effort" y un fallo aqui no debe abortar la operacion principal (aprobar
     * evento, comentar, etc.).
     */
    suspend fun award(userId: String, points: Int) {
        if (userId.isBlank() || points <= 0) return
        runCatching {
            val ref = firestore.collection("users").document(userId)

            // 1) Incremento atomico de puntos.
            ref.set(
                mapOf(
                    "points" to FieldValue.increment(points.toLong()),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()

            // 2) Releer los puntos actualizados para recalcular el nivel.
            val snap = ref.get().await()
            val totalPoints = (snap.getLong("points") ?: 0L).toInt()
            val newLevel = ((totalPoints / 500) + 1).coerceIn(1, 5)

            // 3) Actualizar el campo level (merge para no sobreescribir nada mas).
            ref.set(mapOf("level" to newLevel), SetOptions.merge()).await()
        }
    }
}
