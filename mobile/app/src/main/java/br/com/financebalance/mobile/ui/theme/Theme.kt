package br.com.financebalance.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    onPrimary = OnOrangePrimary,
    primaryContainer = OrangeHover,
    onPrimaryContainer = TextPrimary,
    secondary = AmberSecondary,
    onSecondary = OnOrangePrimary,
    tertiary = CreamTertiary,
    background = CanvasDark,
    onBackground = TextPrimary,
    surface = SurfaceContainerLow,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    outline = BorderStrong,
    outlineVariant = BorderSubtle,
    error = ErrorCoral
)

@Composable
fun FinanceBalanceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
