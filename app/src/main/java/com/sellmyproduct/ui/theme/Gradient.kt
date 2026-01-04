package com.sellmyproduct.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

object Gradient {
    // Primary gradient for splash and main screens
    val primaryGradient = Brush.linearGradient(
        colors = listOf(
            Primary,
            PrimaryVariant,
            Secondary
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    // Secondary gradient for login screen - Full orange coverage
    val secondaryGradient = Brush.linearGradient(
        colors = listOf(
            Primary,
            PrimaryVariant,
            Secondary,
            SecondaryVariant
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    // Light gradient variant
    val lightGradient = Brush.linearGradient(
        colors = listOf(
            Background,
            SurfaceVariant,
            Surface
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )

    // Dark gradient variant
    val darkGradient = Brush.linearGradient(
        colors = listOf(
            BackgroundDark,
            SurfaceDark,
            SurfaceVariantDark
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
}


