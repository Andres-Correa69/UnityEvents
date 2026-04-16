package co.uniquindio.unityevents.features.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.uniquindio.unityevents.core.theme.UnityEventsTheme

/**
 * Pantalla de bienvenida. Es la primera pantalla que ve un usuario sin sesion.
 * Equivalente a la pantalla "Bienvenida - Inicio" del proyecto Stitch.
 *
 * @param onStartClick se invoca al presionar "Empezar" — navega a [RegisterScreen].
 * @param onLoginClick se invoca al presionar "Iniciar sesion" — navega a [LoginScreen].
 */
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circulo decorativo con el icono primario de la app (groups).
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Titulo principal.
            Text(
                text = "Bienvenido a UnityEvents",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Subtitulo descriptivo.
            Text(
                text = "Conecta con tu comunidad y descubre eventos increibles cerca de ti.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Fila de iconos que refuerzan la propuesta de valor (eventos + ubicacion + comunidad).
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                FeatureIcon(icon = Icons.Filled.Event, label = "Eventos")
                FeatureIcon(icon = Icons.Filled.LocationOn, label = "Cerca de ti")
                FeatureIcon(icon = Icons.Filled.Groups, label = "Comunidad")
            }

            Spacer(Modifier.height(48.dp))

            // Boton primario: comienza el flujo de registro.
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Empezar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(12.dp))

            // Boton secundario: acceso a usuarios ya registrados.
            OutlinedButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "Iniciar sesion",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Pequena columna icono + etiqueta usada en la fila de caracteristicas.
 */
@Composable
private fun FeatureIcon(
    icon: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun WelcomeScreenPreview() {
    UnityEventsTheme(dynamicColor = false) {
        WelcomeScreen(onStartClick = {}, onLoginClick = {})
    }
}
