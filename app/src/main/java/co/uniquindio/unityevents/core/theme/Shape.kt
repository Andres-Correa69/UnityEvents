package co.uniquindio.unityevents.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Radios de esquina de la app. Stitch define `roundness = ROUND_FULL`, asi que los
 * componentes principales (botones, inputs, cards) usan radios grandes tipo "pill".
 * Valores alineados con Material 3.
 */
val UnityEventsShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
