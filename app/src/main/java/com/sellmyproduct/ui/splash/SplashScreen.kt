package com.sellmyproduct.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.sellmyproduct.ui.components.WavyBackground
import com.sellmyproduct.ui.theme.Background
import com.sellmyproduct.ui.theme.SellMyProductTheme
import com.sellmyproduct.viewmodel.SplashViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Show splash for 2 seconds
        
        // Check if user is already logged in
        if (viewModel.isUserLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    SplashContent(
        alpha = alphaAnim.value,
        scale = scaleAnim.value
    )
}

@Composable
fun SplashContent(
    alpha: Float,
    scale: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        // Wavy background pattern
        WavyBackground(
            modifier = Modifier.fillMaxSize()
        )
        
        Text(
            text = "SellMyProduct",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        )
    }
}

@Preview(
    name = "Splash Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SplashScreenPreview() {
    SellMyProductTheme {
        SplashContent(
            alpha = 1f,
            scale = 1f
        )
    }
}

@Preview(
    name = "Splash Screen - Animated",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SplashScreenAnimatedPreview() {
    SellMyProductTheme {
        SplashContent(
            alpha = 0.7f,
            scale = 0.8f
        )
    }
}

