package co.uniquindio.unityevents.core.di

import co.uniquindio.unityevents.data.repository.AuthRepositoryImpl
import co.uniquindio.unityevents.data.repository.CommentsRepositoryImpl
import co.uniquindio.unityevents.data.repository.EventsRepositoryImpl
import co.uniquindio.unityevents.data.repository.NotificationsRepositoryImpl
import co.uniquindio.unityevents.data.repository.ProfileRepositoryImpl
import co.uniquindio.unityevents.data.repository.ReportsRepositoryImpl
import co.uniquindio.unityevents.data.repository.TicketsRepositoryImpl
import co.uniquindio.unityevents.domain.repository.AuthRepository
import co.uniquindio.unityevents.domain.repository.CommentsRepository
import co.uniquindio.unityevents.domain.repository.EventsRepository
import co.uniquindio.unityevents.domain.repository.NotificationsRepository
import co.uniquindio.unityevents.domain.repository.ProfileRepository
import co.uniquindio.unityevents.domain.repository.ReportsRepository
import co.uniquindio.unityevents.domain.repository.TicketsRepository
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

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindEventsRepository(impl: EventsRepositoryImpl): EventsRepository

    @Binds @Singleton
    abstract fun bindCommentsRepository(impl: CommentsRepositoryImpl): CommentsRepository

    @Binds @Singleton
    abstract fun bindTicketsRepository(impl: TicketsRepositoryImpl): TicketsRepository

    @Binds @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds @Singleton
    abstract fun bindReportsRepository(impl: ReportsRepositoryImpl): ReportsRepository
}
