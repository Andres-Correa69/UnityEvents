package co.uniquindio.unityevents.core.di

import co.uniquindio.unityevents.data.repository.AuthRepositoryImpl
import co.uniquindio.unityevents.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enlaces Hilt entre interfaces de dominio y sus implementaciones de la capa de datos.
 *
 * Uso de `@Binds` (abstracto) en vez de `@Provides`: es mas eficiente en tiempo de
 * compilacion porque Hilt genera menos codigo — solo es valido cuando la implementacion
 * ya tiene un constructor `@Inject`.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** AuthRepository → AuthRepositoryImpl. */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
