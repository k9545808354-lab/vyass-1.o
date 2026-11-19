package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val VyasBackground = Color(0xFF06090F)
val VyasCardBackground = Color(0xFF0F1624)
val VyasSurfaceElevated = Color(0xFF141B28)
val VyasSurfaceDeep = Color(0xFF090D15)
val VyasBorder = Color(0xFF1C273C)
val VyasBorderStrong = Color(0xFF23344E)
val VyasCyan = Color(0xFF00F0FF)
val VyasGreen = Color(0xFF00FF66)
val VyasYellow = Color(0xFFFEE75C)
val VyasPurple = Color(0xFFC77DFF)
val VyasDeepPurple = Color(0xFF9D00FF)
val VyasRed = Color(0xFFFF3366)
val VyasErrorRed = Color(0xFFFF4560)
val VyasAmber = Color(0xFFFFBD2E)
val VyasTextDim = Color(0xFF8B9BB4)
val VyasTextLight = Color(0xFFFFFFFF)

@Immutable
data class VyasExtendedColors(
    val cardBg: Color = VyasCardBackground,
    val surfaceDeep: Color = VyasSurfaceDeep,
    val surfaceElevated: Color = VyasSurfaceElevated,
    val border: Color = VyasBorder,
    val cyan: Color = VyasCyan,
    val green: Color = VyasGreen,
    val yellow: Color = VyasYellow,
    val purple: Color = VyasPurple,
    val red: Color = VyasRed,
    val amber: Color = VyasAmber,
    val textDim: Color = VyasTextDim
)

val LocalVyasColors = staticCompositionLocalOf { VyasExtendedColors() }

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

private val VyasDarkColorScheme = darkColorScheme(
    primary = VyasCyan,
    onPrimary = Color(0xFF070A10),
    primaryContainer = Color(0xFF0A2A38),
    onPrimaryContainer = VyasCyan,
    secondary = VyasGreen,
    onSecondary = Color(0xFF04140A),
    secondaryContainer = Color(0xFF0A2E1B),
    onSecondaryContainer = VyasGreen,
    tertiary = VyasPurple,
    onTertiary = Color(0xFF1A0629),
    background = VyasBackground,
    onBackground = VyasTextLight,
    surface = VyasCardBackground,
    onSurface = VyasTextLight,
    surfaceVariant = VyasSurfaceElevated,
    onSurfaceVariant = VyasTextDim,
    outline = VyasBorder,
    error = VyasErrorRed,
    onError = VyasTextLight
)

val VyasTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun VyasTheme(content: @Composable () -> Unit) {
    val extendedColors = VyasExtendedColors()
    CompositionLocalProvider(LocalVyasColors provides extendedColors) {
        MaterialTheme(
            colorScheme = VyasDarkColorScheme,
            typography = VyasTypography,
            content = content
        )
    }
}
