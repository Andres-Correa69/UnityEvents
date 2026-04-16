package co.uniquindio.unityevents.core.di

import android.content.Context
import androidx.credentials.CredentialManager
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
}
