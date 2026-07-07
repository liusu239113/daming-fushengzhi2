package com.daming.fushengzhi2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Scheme = lightColorScheme(
    primary = MingColors.Primary,
    onPrimary = MingColors.BgWhite,
    secondary = MingColors.Gold,
    background = MingColors.BgLight,
    surface = MingColors.BgWhite,
    onBackground = MingColors.TextPrimary,
    onSurface = MingColors.TextPrimary,
    error = MingColors.Red
)

@Composable
fun MingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
