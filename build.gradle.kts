//
// Top-level build file — registra los plugins que se aplicaran en modulos hijos.
// Aqui NO se ejecutan (apply false); cada submodulo (ej. :app) los activa cuando los necesita.
//
plugins {
    // Android + Kotlin base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Compose compiler plugin (requerido desde Kotlin 2.0)
    alias(libs.plugins.kotlin.compose) apply false

    // KSP: procesador de anotaciones usado por Hilt
    alias(libs.plugins.ksp) apply false

    // Hilt: inyeccion de dependencias
    alias(libs.plugins.hilt.android.plugin) apply false

    // Google Services: lee app/google-services.json y expone la config de Firebase
    alias(libs.plugins.google.services) apply false
}
