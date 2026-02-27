package co.uniquindio.unityevents.core.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Boton primario reutilizable con soporte para estado de carga.
 *
 * Muestra un indicador de progreso circular cuando [isLoading] es true,
 * reemplazando el texto del boton y deshabilitando la interaccion.
 *
 * @param text Texto que se muestra en el boton.
 * @param onClick Callback invocado cuando el usuario presiona el boton.
 * @param modifier Modificador de Compose para personalizar el layout.
 * @param isLoading Indica si el boton esta en estado de carga.
 * @param enabled Indica si el boton esta habilitado para interaccion.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        // Deshabilitar el boton si esta cargando o si esta explicitamente deshabilitado
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            // Mostrar indicador de progreso durante la carga
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cargando...")
        } else {
            // Mostrar el texto normal del boton
            Text(text = text)
        }
    }
}
