package io.github.takano536.kobako.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class KobakoColors(
    val paper: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val buttonPrimary: Color,
    val buttonDanger: Color,
    val moneyNegative: Color,
    val moneyPositive: Color,
    val categoryDot: Color,
    val categoryBar: Color,
    val rule: Color,
    val bar: Color,
    val surface: Color,
    val surfaceHover: Color,
    val softControl: Color,
    val onAccent: Color,
)

private val LightColors = KobakoColors(
    paper = Color(0xfffcfcf8), ink = Color(0xff292e2b), muted = Color(0xff68736c),
    accent = Color(0xff426d59), buttonPrimary = Color(0xff2f6b52), buttonDanger = Color(0xffa84f45),
    moneyNegative = Color(0xffa33a36), moneyPositive = Color(0xff245c9c), categoryDot = Color(0xff91a99d),
    categoryBar = Color(0xff8da597), rule = Color(0xffdfe3da), bar = Color(0xffe5e9e1),
    surface = Color(0xfff8faf5), surfaceHover = Color(0xffedf2eb), softControl = Color(0xfff0f2eb), onAccent = Color.White,
)

private val DarkColors = KobakoColors(
    paper = Color(0xff1a1f1c), ink = Color(0xffe4e7e0), muted = Color(0xffa2ada5), accent = Color(0xff8cc4a6),
    buttonPrimary = Color(0xff8cc4a6), buttonDanger = Color(0xffec948a), moneyNegative = Color(0xfff2978e),
    moneyPositive = Color(0xff8db9ef), categoryDot = Color(0xff91a99d), categoryBar = Color(0xff6f8a7c),
    rule = Color(0xff343d37), bar = Color(0xff2b332e), surface = Color(0xff1f2521), surfaceHover = Color(0xff28302b),
    softControl = Color(0xff262d28), onAccent = Color(0xff142019),
)

private val LocalKobakoColors = staticCompositionLocalOf { LightColors }

/** Theme-scoped design tokens; screen code must never choose light/dark constants itself. */
object ThemeTokens {
    val colors: KobakoColors
        @Composable get() = LocalKobakoColors.current
}

private val LightScheme = lightColorScheme(
    primary = LightColors.accent,
    onPrimary = LightColors.onAccent,
    background = LightColors.paper,
    onBackground = LightColors.ink,
    surface = LightColors.surface,
    onSurface = LightColors.ink,
    outline = LightColors.rule,
)
private val DarkScheme = darkColorScheme(
    primary = DarkColors.accent,
    onPrimary = DarkColors.onAccent,
    background = DarkColors.paper,
    onBackground = DarkColors.ink,
    surface = DarkColors.surface,
    onSurface = DarkColors.ink,
    outline = DarkColors.rule,
)

@Composable
fun KobakoTheme(theme: String, content: @Composable () -> Unit) {
    val dark = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalKobakoColors provides if (dark) DarkColors else LightColors) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = Typography(), content = content)
    }
}

val SectionSpace = 32.dp
