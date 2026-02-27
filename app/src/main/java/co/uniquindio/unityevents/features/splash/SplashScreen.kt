package co.uniquindio.unityevents.features.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.uniquindio.unityevents.R
import kotlinx.coroutines.delay

/**
 * Pantalla de bienvenida (Splash) de la aplicacion UnityEvents.
 *
 * Muestra el logo de la aplicacion con una animacion de aparicion
 * gradual (fade-in) y luego navega automaticamente a la pantalla
 * de login despues de un breve periodo.
 *
 * @param onSplashFinished Callback invocado cuando la animacion termina y se debe navegar.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Estado para controlar el inicio de la animacion de fade-in
    var startAnimation by remember { mutableStateOf(false) }

    // Animacion de opacidad: de 0 (invisible) a 1 (visible) en 1.2 segundos
    val alphaAnimation by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "splashAlpha"
    )

    // Iniciar la animacion y programar la navegacion despues de 2.5 segundos
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500L)
        onSplashFinished()
    }

    // Contenido de la pantalla splash con fondo del tema
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo de la aplicacion con animacion de fade-in
        Image(
            painter = painterResource(id = R.drawable.logo_unity_events),
            contentDescription = "Logo UnityEvents",
            modifier = Modifier
                .size(280.dp)
                .alpha(alphaAnimation)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nombre de la aplicacion con animacion de fade-in
        Text(
            text = "UnityEvents",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(alphaAnimation)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitulo con animacion de fade-in
        Text(
            text = "Conecta con tus eventos",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(alphaAnimation)
        )
    }
}
