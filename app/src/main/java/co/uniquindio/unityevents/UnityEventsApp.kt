package co.uniquindio.unityevents

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase [Application] de UnityEvents.
 *
 * La anotacion `@HiltAndroidApp` detona la generacion del contenedor de dependencias
 * a nivel de aplicacion; a partir de aqui las Activities/Fragments/ViewModels marcados
 * con `@AndroidEntryPoint` o `@HiltViewModel` pueden recibir sus dependencias.
 */
@HiltAndroidApp
class UnityEventsApp : Application()
