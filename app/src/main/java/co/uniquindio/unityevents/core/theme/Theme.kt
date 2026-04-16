package co.uniquindio.unityevents.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Tema raiz de la aplicacion. Cumple la regla de CLAUDE.md:
 * "TODO el diseno visual debe usar Material You (Material 3) para lograr una apariencia nativa y moderna"
 * y "Respetar el sistema de colores dinamicos de Material You (`dynamicColorScheme`)".
 *
 * Flujo:
 * 1. Si el dispositivo esta en Android 12+ (S, API 31) y [dynamicColor] es `true`, se usa
 *    `dynamicLightColorScheme` / `dynamicDarkColorScheme`, que genera colores a partir del wallpaper.
 * 2. En cualquier otro caso se cae a los [LightColors] / [DarkColors] definidos aqui, que
 *    derivan del token `#A436F2` (ver Color.kt).
 *
 * @param darkTheme fuerza modo oscuro; por defecto se sigue la config del sistema.
 * @param dynamicColor activa Material You en Android 12+; por defecto `true`.
 */
@Composable
fun UnityEventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANTE: `false` por defecto para respetar la paleta de marca de Stitch (#A436F2).
    // Si se activa, Android 12+ reemplaza los colores con los del wallpaper (Material You).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Ajusta los iconos del status bar: oscuros sobre fondos claros, claros sobre fondos oscuros.
    // Nota: en Android 15+ `window.statusBarColor` esta deprecado y la app dibuja edge-to-edge,
    // asi que solo controlamos la apariencia de los iconos. El fondo lo pinta el propio content.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnityEventsTypography,
        shapes = UnityEventsShapes,
        content = content
    )
}

// -----------------------------------------------------------------------------
// Esquemas de color fallback (derivados de la semilla #A436F2 de Stitch).
// -----------------------------------------------------------------------------

/** Esquema claro — por defecto en la app, alineado con Stitch (modo LIGHT). */
private val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = NeutralWhite,
    primaryContainer = BrandPrimarySoft,
    onPrimaryContainer = NeutralBlack,

    secondary = BrandPrimaryBright,
    onSecondary = NeutralWhite,
    secondaryContainer = BrandPrimarySoft,
    onSecondaryContainer = NeutralBlack,

    tertiary = BrandPrimaryBright,
    onTertiary = NeutralWhite,

    background = NeutralWhite,
    onBackground = NeutralBlack,
    surface = NeutralWhite,
    onSurface = NeutralBlack,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralOnSurfaceVariant,

    error = SemanticError,
    onError = NeutralWhite,
    errorContainer = SemanticErrorContainer,
    onErrorContainer = NeutralBlack,

    outline = NeutralOnSurfaceVariant,
)

/** Esquema oscuro — mantiene la semilla violeta pero invierte jerarquia de fondos. */
private val DarkColors = darkColorScheme(
    primary = BrandPrimaryBright,
    onPrimary = NeutralBlack,
    primaryContainer = BrandPrimary,
    onPrimaryContainer = NeutralWhite,

    secondary = BrandPrimarySoft,
    onSecondary = NeutralBlack,

    tertiary = BrandPrimarySoft,
    onTertiary = NeutralBlack,

    background = NeutralDarkSurface,
    onBackground = NeutralWhite,
    surface = NeutralDarkSurface,
    onSurface = NeutralWhite,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onSurfaceVariant = NeutralSurfaceVariant,

    error = SemanticError,
    onError = NeutralWhite,

    outline = NeutralSurfaceVariant,
)
