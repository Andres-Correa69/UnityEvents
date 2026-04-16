package co.uniquindio.unityevents.core.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Barra de estrellas (1..5). Si [onRatingChanged] es null, se muestra solo-lectura;
 * si no, al tocar una estrella se dispara el callback con la nueva calificacion.
 */
@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    size: Int = 24,
    onRatingChanged: ((Int) -> Unit)? = null
) {
    Row(modifier = modifier) {
        for (i in 1..5) {
            val filled = i <= rating
            val starColor = if (filled) MaterialTheme.colorScheme.primary else Color.Gray
            val starIcon = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder
            val starModifier = Modifier
                .size(size.dp)
                .let { base -> if (onRatingChanged != null) base.clickable { onRatingChanged(i) } else base }

            Icon(
                imageVector = starIcon,
                contentDescription = "$i estrellas",
                tint = starColor,
                modifier = starModifier
            )
        }
    }
}
