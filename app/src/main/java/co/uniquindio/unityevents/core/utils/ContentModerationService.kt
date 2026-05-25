package co.uniquindio.unityevents.core.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import co.uniquindio.unityevents.domain.model.ModerationResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FinishReason
import com.google.ai.client.generativeai.type.PromptBlockedException
import com.google.ai.client.generativeai.type.ResponseStoppedException
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de moderacion automatica de contenido para eventos.
 *
 * Llama a Gemini con un prompt multimodal (titulo + descripcion + imagen opcional) y le
 * pide que clasifique el contenido como apropiado o inapropiado segun categorias prohibidas
 * (pornografia, violencia, insultos, odio, drogas ilegales, fraude, acoso).
 *
 * El modelo responde JSON estricto del tipo `{"approved": bool, "reasons": [string]}`.
 *
 * IMPORTANTE: Esta es solo la PRIMERA capa de filtrado. El moderador humano sigue siendo
 * el filtro final. Por eso aplicamos "fail-open": si la IA falla por cualquier motivo
 * (sin red, timeout, JSON invalido, etc.) dejamos pasar el evento para que el moderador
 * humano decida — es preferible eso a impedir publicar eventos legitimos por fallas
 * tecnicas.
 */
@Singleton
class ContentModerationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val generativeModel: GenerativeModel
) {
    /**
     * Analiza el contenido del evento y devuelve si pasa el filtro o no.
     *
     * @param title Titulo del evento.
     * @param description Descripcion del evento.
     * @param imageUri URI de la imagen del evento (Photo Picker / Storage). Si es null
     *                 solo se analiza el texto.
     */
    suspend fun analyze(
        title: String,
        description: String,
        imageUri: Uri?
    ): ModerationResult = withContext(Dispatchers.IO) {
        Log.i(TAG, "Analizando evento -> titulo=\"${title.take(40)}\", conImagen=${imageUri != null}")
        try {
            // Construimos el contenido multimodal: prompt + (opcionalmente) imagen.
            val bitmap = imageUri?.let { loadBitmapDownsampled(it) }
            val response = generativeModel.generateContent(
                content {
                    text(buildPrompt(title, description))
                    bitmap?.let { image(it) }
                }
            )

            // CASO ESPECIAL: aunque pongamos los safety settings en NONE, Gemini tiene
            // filtros DE BAJO NIVEL para CSAM y pornografia explicita que NO se pueden
            // desactivar. Cuando los disparas, la respuesta viene con blockReason o
            // finishReason=SAFETY. Esto NO es un error tecnico: es Gemini diciendo
            // "este contenido es claramente prohibido". Lo tratamos como bloqueo.
            val promptBlock = response.promptFeedback?.blockReason
            if (promptBlock != null) {
                Log.i(TAG, "✗ Gemini bloqueo el prompt por safety filter: $promptBlock")
                return@withContext blockedBySafety()
            }
            val finishReason = response.candidates.firstOrNull()?.finishReason
            if (finishReason == FinishReason.SAFETY) {
                Log.i(TAG, "✗ Gemini detuvo la respuesta por safety filter: $finishReason")
                return@withContext blockedBySafety()
            }

            val raw = response.text?.trim().orEmpty()
            if (raw.isEmpty()) {
                Log.w(TAG, "Gemini devolvio respuesta vacia. Fail-open al moderador humano.")
                return@withContext ModerationResult.failOpen()
            }
            Log.i(TAG, "Gemini respondio: $raw")
            val parsed = parseJson(raw)
            if (parsed.approved) {
                Log.i(TAG, "✓ IA aprueba el contenido. Continua al moderador humano.")
            } else {
                Log.i(TAG, "✗ IA bloquea el contenido. Motivos: ${parsed.reasons.joinToString(", ")}")
            }
            parsed
        } catch (e: PromptBlockedException) {
            // Gemini rechazo el prompt por considerarlo prohibido (tipico de imagenes
            // sexuales explicitas que disparan el filtro hardcoded de Google).
            Log.i(TAG, "✗ PromptBlockedException de Gemini: ${e.message}")
            blockedBySafety()
        } catch (e: ResponseStoppedException) {
            // Gemini empezo a responder pero se detuvo por safety.
            Log.i(TAG, "✗ ResponseStoppedException de Gemini: ${e.message}")
            blockedBySafety()
        } catch (e: Throwable) {
            // Cualquier OTRA excepcion -> fail-open. NO bloqueamos al usuario por fallas
            // tecnicas (sin red, timeout, JSON invalido, etc.).
            Log.w(TAG, "⚠ Fallo el servicio de moderacion IA (${e.javaClass.simpleName}: ${e.message}). Fail-open al moderador humano.", e)
            ModerationResult.failOpen()
        }
    }

    /**
     * Resultado para cuando el propio modelo bloquea la peticion por safety filter.
     * Mensaje generico pero util: el usuario sabe que la IA detecto algo y debe revisar
     * (especialmente la imagen, que es el caso mas comun).
     */
    private fun blockedBySafety(): ModerationResult = ModerationResult(
        approved = false,
        reasons = listOf(
            "La IA detecto contenido prohibido (sexual explicito, violento o similar). " +
                "Revisa especialmente la imagen del evento."
        )
    )

    /**
     * Prompt en espanol que define las categorias prohibidas y exige respuesta JSON pura.
     * Se mantiene en una sola cadena para que el modelo no se confunda con instrucciones
     * dispersas.
     */
    private fun buildPrompt(title: String, description: String): String = """
        Eres un moderador automatico de contenido para UnityEvents, una app de eventos de
        una universidad publica colombiana. Analiza el siguiente evento y determina si
        contiene material prohibido en cualquiera de estas categorias:

        - Pornografia, desnudez explicita o contenido sexual
        - Violencia grafica o explicita
        - Insultos, lenguaje ofensivo o vulgar
        - Discurso de odio (racismo, xenofobia, homofobia, etc.)
        - Drogas ilegales o promocion de su consumo
        - Actividades fraudulentas, estafas o piramides
        - Acoso, amenazas o doxxing

        Si la imagen adjunta (cuando exista) muestra cualquiera de las anteriores, marca
        el evento como NO aprobado.

        EVENTO A ANALIZAR
        Titulo: ${title.trim()}
        Descripcion: ${description.trim()}

        Responde EXCLUSIVAMENTE con JSON valido sin markdown ni texto extra, con este formato:
        {"approved": boolean, "reasons": ["motivo legible en espanol", ...]}

        Si todo es apropiado: {"approved": true, "reasons": []}
        Si NO es apropiado: {"approved": false, "reasons": ["..."]} con uno o mas motivos
        cortos y especificos en espanol que el usuario pueda entender para editar su evento.
    """.trimIndent()

    /**
     * Parsea el JSON de Gemini. Tolera respuestas envueltas en backticks tipo markdown
     * (```json ... ```) que algunas versiones del modelo agregan a pesar de la instruccion.
     */
    private fun parseJson(raw: String): ModerationResult {
        // Si vino envuelto en ```json ... ``` lo limpiamos para que JSONObject no falle.
        val cleaned = raw
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```")
            .trim()
        val json = JSONObject(cleaned)
        val approved = json.optBoolean("approved", true)
        val reasonsArray = json.optJSONArray("reasons")
        val reasons: List<String> = if (reasonsArray == null) {
            emptyList()
        } else {
            buildList {
                for (i in 0 until reasonsArray.length()) {
                    val item = reasonsArray.optString(i).trim()
                    if (item.isNotBlank()) add(item)
                }
            }
        }
        return ModerationResult(approved = approved, reasons = reasons)
    }

    /**
     * Carga la imagen reduciendo memoria a ~1/4 del tamano original para no agotar RAM en
     * dispositivos con poca memoria (las fotos del telefono suelen ser de 12+ MP).
     * Gemini Flash funciona bien con imagenes de baja resolucion para este caso de uso.
     */
    private fun loadBitmapDownsampled(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply { inSampleSize = 4 }
            BitmapFactory.decodeStream(stream, null, options)
        }
    }.getOrNull()

    private companion object {
        const val TAG = "ContentModeration"
    }
}
