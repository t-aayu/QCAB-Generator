package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class QcabExtendedColors(
    val exportAction: Color,
    val onExportAction: Color,
    val exportContainer: Color,
    val onExportContainer: Color,
    val caution: Color,
    val onCaution: Color,
    val cautionContainer: Color,
    val onCautionContainer: Color,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val heroText: Color,
    val heroTextMuted: Color,
    val paperSheet: Color,
    val paperInk: Color
)

private val LightExtendedColors = QcabExtendedColors(
    exportAction = SealEmerald,
    onExportAction = OnSealEmerald,
    exportContainer = SealEmeraldContainer,
    onExportContainer = OnSealEmeraldContainer,
    caution = CautionAmber,
    onCaution = OnCautionAmber,
    cautionContainer = CautionAmberContainer,
    onCautionContainer = OnCautionAmberContainer,
    heroStart = HeaderSlateDeep,
    heroMid = HeaderSlateMid,
    heroEnd = HeaderIndigoTint,
    heroText = HeaderTextPrimary,
    heroTextMuted = HeaderTextMuted,
    paperSheet = PaperSurface,
    paperInk = SlateInk
)

private val DarkExtendedColors = QcabExtendedColors(
    exportAction = DarkSealEmerald,
    onExportAction = DarkOnSealEmerald,
    exportContainer = DarkSealEmeraldContainer,
    onExportContainer = DarkOnSealEmeraldContainer,
    caution = DarkCautionAmber,
    onCaution = DarkOnCautionAmber,
    cautionContainer = DarkCautionAmberContainer,
    onCautionContainer = DarkOnCautionAmberContainer,
    heroStart = HeaderSlateDeep,
    heroMid = HeaderIndigoTint,
    heroEnd = HeaderSlateMid,
    heroText = HeaderTextPrimary,
    heroTextMuted = HeaderTextMuted,
    paperSheet = PaperSurface,
    paperInk = SlateInk
)

val LocalQcabColors = staticCompositionLocalOf { LightExtendedColors }

private val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = OnInkPrimary,
    primaryContainer = InkPrimaryContainer,
    onPrimaryContainer = OnInkPrimaryContainer,
    secondary = SealEmerald,
    onSecondary = OnSealEmerald,
    secondaryContainer = SealEmeraldContainer,
    onSecondaryContainer = OnSealEmeraldContainer,
    tertiary = CautionAmber,
    onTertiary = OnCautionAmber,
    tertiaryContainer = CautionAmberContainer,
    onTertiaryContainer = OnCautionAmberContainer,
    background = PaperBackground,
    onBackground = SlateInk,
    surface = PaperSurface,
    onSurface = SlateInk,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = SlateInkMuted,
    outline = PaperOutline,
    outlineVariant = PaperOutlineVariant,
    error = ErrorCrimson,
    onError = Color.White,
    errorContainer = ErrorCrimsonContainer,
    onErrorContainer = OnErrorCrimsonContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInkPrimary,
    onPrimary = DarkOnInkPrimary,
    primaryContainer = DarkInkPrimaryContainer,
    onPrimaryContainer = DarkOnInkPrimaryContainer,
    secondary = DarkSealEmerald,
    onSecondary = DarkOnSealEmerald,
    secondaryContainer = DarkSealEmeraldContainer,
    onSecondaryContainer = DarkOnSealEmeraldContainer,
    tertiary = DarkCautionAmber,
    onTertiary = DarkOnCautionAmber,
    tertiaryContainer = DarkCautionAmberContainer,
    onTertiaryContainer = DarkOnCautionAmberContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = Color.Black,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

val QcabShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun QcabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalQcabColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QcabTypography,
            shapes = QcabShapes,
            content = content
        )
    }
}
