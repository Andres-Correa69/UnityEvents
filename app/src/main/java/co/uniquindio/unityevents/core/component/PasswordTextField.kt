package co.uniquindio.unityevents.core.component

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Campo de texto reutilizable para contrasenas con funcionalidad
 * de mostrar/ocultar el texto ingresado.
 *
 * Utiliza [OutlinedTextField] de Material 3 con un icono de ojo
 * que permite alternar la visibilidad de la contrasena.
 *
 * @param value Valor actual del campo de texto.
 * @param onValueChange Callback invocado cuando el usuario modifica el texto.
 * @param label Etiqueta descriptiva del campo.
 * @param isVisible Indica si la contrasena es visible (texto plano) u oculta (puntos).
 * @param onVisibilityToggle Callback invocado cuando el usuario presiona el icono de visibilidad.
 * @param modifier Modificador de Compose para personalizar el layout.
 * @param isError Indica si el campo debe mostrarse en estado de error.
 * @param supportingText Texto de apoyo que se muestra debajo del campo (usado para mensajes de error).
 * @param imeAction Accion del teclado (por defecto [ImeAction.Next]).
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        modifier = modifier,
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        // Transformacion visual: ocultar o mostrar la contrasena
        visualTransformation = if (isVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        // Configuracion del teclado para contrasenas
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        // Icono para alternar la visibilidad de la contrasena
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = if (isVisible) {
                        "Ocultar contrasena"
                    } else {
                        "Mostrar contrasena"
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
