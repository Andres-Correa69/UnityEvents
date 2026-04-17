//
// Build script del modulo :app
//
// - Activa Jetpack Compose (buildFeatures.compose = true)
// - Aplica Hilt + KSP para inyeccion de dependencias
// - Aplica el plugin google-services (lee app/google-services.json)
// - Expone la variable WEB_CLIENT_ID desde local.properties como BuildConfig,
//   necesaria para Google Sign-In via Credential Manager.
//
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android.plugin)
    // `google-services` se aplica CONDICIONALMENTE al final del archivo:
    // solo si existe `app/google-services.json`. Asi el proyecto compila aunque el
    // desarrollador todavia no haya descargado el archivo desde Firebase Console.
    // Ver docs/FIREBASE_SETUP.md para descargarlo.
}

// Carga local.properties para leer claves que NO deben ser commiteadas
// (por ejemplo el WEB_CLIENT_ID de Google Sign-In). Si el archivo no existe,
// o la llave falta, se usa un valor vacio para que el build no se rompa.
val localProperties = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        propsFile.inputStream().use { load(it) }
    }
}
val webClientId: String = localProperties.getProperty("WEB_CLIENT_ID", "")
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY", "")

android {
    namespace = "co.uniquindio.unityevents"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.uniquindio.unityevents"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Expone el Web Client ID (Firebase -> Auth -> Google) como constante en BuildConfig.
        // Asi nunca queda hardcodeado en el codigo fuente; se lee de local.properties.
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")

        // Inyecta la Maps API key al AndroidManifest (meta-data com.google.android.geo.API_KEY).
        // El valor se lee de local.properties (NO commiteado).
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    // Compose compiler lo provee el plugin kotlin-compose, no hay que configurar composeOptions.
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- AndroidX base + Lifecycle ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // --- Compose (via BOM) ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts) // GoogleFont provider (fonts descargables)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- Navegacion ---
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services) // tasks.await() en Firebase

    // --- Hilt ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // --- Firebase (BOM) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // --- Google Sign-In via Credential Manager ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // --- Imagenes (Coil) ---
    implementation(libs.coil.compose)

    // --- QR ---
    implementation(libs.zxing.core)

    // --- Camara + ML Kit Barcode (escaner de QR en tiempo real) ---
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.accompanist.permissions)
    // Guava provee ListenableFuture, tipo de retorno de ProcessCameraProvider.getInstance().
    // Sin esta dep, Kotlin no puede resolver .addListener() / .get() sobre el future.
    implementation(libs.guava)

    // --- Google Maps + Location ---
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // --- Tests unitarios ---
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // --- Tests instrumentados ---
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

// -----------------------------------------------------------------------------
// Plugin google-services: aplicarlo solo si existe `app/google-services.json`.
// -----------------------------------------------------------------------------
// El plugin `com.google.gms.google-services` necesita este archivo para funcionar;
// si no esta, falla el build. Lo aplicamos condicionalmente para que el proyecto
// compile aunque el desarrollador aun no haya completado la config de Firebase.
// Una vez descargado el archivo desde Firebase Console, el plugin se activa en el
// siguiente build.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn(
        "[UnityEvents] 'app/google-services.json' no encontrado. Firebase NO se " +
            "inicializara en runtime. Sigue docs/FIREBASE_SETUP.md para descargarlo."
    )
}
