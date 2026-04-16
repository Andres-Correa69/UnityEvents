package co.uniquindio.unityevents.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta base de UnityEvents extraida de la guia de estilo de Stitch (proyecto 12495687145835338723).
 *
 * El color protagonico es un violeta Material You `#A436F2`. Sobre esa semilla se construyen
 * los esquemas claro y oscuro que usa [UnityEventsTheme]. En dispositivos con Android 12+ la app
 * prefiere `dynamicColorScheme` (colores derivados del wallpaper del usuario), y cae a estos
 * tokens cuando la API no lo soporta.
 */

// --- Tokens de marca (brand) -------------------------------------------------
/** Color primario principal — violeta "UnityEvents". */
val BrandPrimary = Color(0xFFA436F2)

/** Variante brillante del primario, usada en estados hover/enfasis. */
val BrandPrimaryBright = Color(0xFFC084FC)

/** Variante suave del primario — fondos y chips ligeros. */
val BrandPrimarySoft = Color(0xFFE9D5FF)

// --- Neutros -----------------------------------------------------------------
/** Blanco puro — superficie principal en modo claro. */
val NeutralWhite = Color(0xFFFFFFFF)

/** Negro casi puro para texto sobre fondos claros. */
val NeutralBlack = Color(0xFF0B0B0E)

/** Gris muy claro para contenedores y separadores. */
val NeutralSurfaceVariant = Color(0xFFF4F1F8)

/** Gris medio para textos secundarios / iconos inactivos. */
val NeutralOnSurfaceVariant = Color(0xFF5A5866)

/** Gris oscuro para superficies en modo oscuro. */
val NeutralDarkSurface = Color(0xFF141218)

/** Gris oscuro elevado (cards en modo oscuro). */
val NeutralDarkSurfaceVariant = Color(0xFF2B2930)

// --- Semanticos --------------------------------------------------------------
/** Rojo para errores de validacion y destrucciones. */
val SemanticError = Color(0xFFBA1A1A)

/** Fondo suave para mensajes de error. */
val SemanticErrorContainer = Color(0xFFFFDAD6)
