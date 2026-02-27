package co.uniquindio.unityevents.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Esquema de colores para el tema claro.
 * Se usa como fallback cuando los colores dinamicos no estan disponibles.
 */
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

/**
 * Esquema de colores para el tema oscuro.
 * Se usa como fallback cuando los colores dinamicos no estan disponibles.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

/**
 * Tema principal de la aplicacion UnityEvents.
 *
 * Implementa Material You (Material 3) con soporte para:
 * - Colores dinamicos del sistema (Android 12+)
 * - Tema claro y oscuro automatico
 * - Tipografia personalizada
 *
 * @param darkTheme Indica si se debe usar el tema oscuro. Por defecto sigue la configuracion del sistema.
 * @param dynamicColor Indica si se deben usar colores dinamicos del sistema. Deshabilitado por defecto para mantener la identidad visual morada.
 * @param content Contenido composable que sera envuelto por el tema.
 */
@Composable
fun UnityEventsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Seleccionar el esquema de colores segun la configuracion
    val colorScheme = when {
        // Usar colores dinamicos si estan habilitados y disponibles (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Usar esquema oscuro personalizado con morados profundos
        darkTheme -> DarkColorScheme
        // Usar esquema claro personalizado con morados pastel
        else -> LightColorScheme
    }

    // Aplicar el tema Material 3 con los esquemas de colores y tipografia
    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnityEventsTypography,
        content = content
    )
}
