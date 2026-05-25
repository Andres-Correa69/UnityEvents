package co.uniquindio.unityevents.core.di

import android.content.Context
import androidx.credentials.CredentialManager
import co.uniquindio.unityevents.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt que expone los clientes de Firebase y del Credential Manager como singletons.
 * Instalado en [SingletonComponent] para que las instancias vivan toda la app.
 *
 * Las instancias se crean vez por proceso y se reutilizan en todos los repositorios / helpers.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Cliente de Firebase Authentication. */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    /** Cliente de Cloud Firestore (base de datos documental NoSQL). */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    /**
     * Cliente de Firebase Storage. No se usa en Fase A pero queda inyectable
     * para que las features posteriores (fotos de perfil y eventos) lo usen sin friccion.
     */
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    /**
     * Credential Manager del sistema — usado por `GoogleSignInHelper`.
     * Se instancia a partir del `ApplicationContext` pero los `getCredential()` requieren
     * un contexto de Activity (se pasa en el sitio de uso).
     */
    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext context: Context
    ): CredentialManager = CredentialManager.create(context)

    /**
     * Modelo Gemini usado por el servicio de moderacion de contenido. Configuracion:
     *
     * - `gemini-2.5-flash`: rapido y multimodal (texto + imagen), incluido en el free tier.
     *   IMPORTANTE: gemini-1.5-flash fue retirado en sept/2025; gemini-2.0-flash cierra el
     *   1 de junio de 2026. Si vuelve a fallar con 404, revisar
     *   https://ai.google.dev/gemini-api/docs/models y actualizar al modelo vigente.
     * - `responseMimeType = application/json`: forzamos respuesta JSON pura para parsear sin
     *   ambiguedades.
     * - `temperature = 0.2`: baja para clasificacion (queremos consistencia, no creatividad).
     * - Safety settings en NONE: el modelo por defecto BLOQUEA su respuesta cuando detecta
     *   contenido ofensivo en la entrada — pero nosotros QUEREMOS que lo analice y clasifique
     *   (no que se rehuse a responder), por eso bajamos los filtros. La moderacion la hace
     *   nuestro propio prompt, no la heuristica interna de Gemini.
     *
     * Requiere `GEMINI_API_KEY` en local.properties (ver app/build.gradle.kts).
     */
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            temperature = 0.2f
        },
        safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.NONE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
        )
    )
}
