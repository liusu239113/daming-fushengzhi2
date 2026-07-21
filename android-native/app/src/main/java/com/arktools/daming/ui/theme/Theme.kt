package com.arktools.daming.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.arktools.daming.R

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

private val PixelFont = FontFamily(
    Font(R.font.fusion_pixel_prop, FontWeight.Normal),
    Font(R.font.fusion_pixel_prop_bold, FontWeight.Bold)
)

private val ThickHeiFont = FontFamily(
    Font(R.font.noto_sans_sc_black, FontWeight.Normal),
    Font(R.font.noto_sans_sc_black, FontWeight.Bold)
)

private val MingSerifFont = FontFamily(
    Font(R.font.noto_serif_cjk_sc_bold, FontWeight.Normal),
    Font(R.font.noto_serif_cjk_sc_bold, FontWeight.Bold)
)

private val CraftMonoFont = FontFamily(
    Font(R.font.dejavu_sans_mono, FontWeight.Normal),
    Font(R.font.dejavu_sans_mono_bold, FontWeight.Bold)
)

fun fontFamilyFor(style: FontStyleKey): FontFamily = when (style) {
    FontStyleKey.Pixel -> PixelFont
    FontStyleKey.Hei -> ThickHeiFont
    FontStyleKey.Serif -> MingSerifFont
    FontStyleKey.Mono -> CraftMonoFont
}

private fun typographyFor(style: FontStyleKey): Typography {
    val family = fontFamilyFor(style)
    val base = Typography()
    fun TextStyle.withGameFont() = copy(fontFamily = family)
    return Typography(
        displayLarge = base.displayLarge.withGameFont(),
        displayMedium = base.displayMedium.withGameFont(),
        displaySmall = base.displaySmall.withGameFont(),
        headlineLarge = base.headlineLarge.withGameFont(),
        headlineMedium = base.headlineMedium.withGameFont(),
        headlineSmall = base.headlineSmall.withGameFont(),
        titleLarge = base.titleLarge.withGameFont(),
        titleMedium = base.titleMedium.withGameFont(),
        titleSmall = base.titleSmall.withGameFont(),
        bodyLarge = base.bodyLarge.withGameFont(),
        bodyMedium = base.bodyMedium.withGameFont(),
        bodySmall = base.bodySmall.withGameFont(),
        labelLarge = base.labelLarge.withGameFont(),
        labelMedium = base.labelMedium.withGameFont(),
        labelSmall = base.labelSmall.withGameFont()
    )
}

@Composable
fun MingTheme(fontStyle: FontStyleKey = FontStyleKey.Hei, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = typographyFor(fontStyle), content = content)
}
