package io.github.takano536.kobako.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class KobakoColors(
    val paper: Color,
    val ink: Color,
    val muted: Color,
    val accent: Color,
    val accentStrong: Color,
    val buttonPrimary: Color,
    val buttonDanger: Color,
    val moneyNegative: Color,
    val moneyPositive: Color,
    val categoryDot: Color,
    val categoryBar: Color,
    val categoryDots: List<Color>,
    val rule: Color,
    val bar: Color,
    val chipSelected: Color,
    val surface: Color,
    val surfaceHover: Color,
    val softControl: Color,
    val onAccent: Color,
)

private val LightColors = KobakoColors(
    paper = Color(0xfffcfcf8), ink = Color(0xff292e2b), muted = Color(0xff68736c),
    accent = Color(0xff426d59), accentStrong = Color(0xff315944), buttonPrimary = Color(0xff2f6b52), buttonDanger = Color(0xffa84f45),
    moneyNegative = Color(0xffa33a36), moneyPositive = Color(0xff426d59), categoryDot = Color(0xff91a99d), categoryBar = Color(0xff8da597),
    categoryDots = listOf(Color(0xff91a99d), Color(0xffb49a96), Color(0xffb0a784), Color(0xffb09eaf), Color(0xff98a6b0), Color(0xffa4b2a9), Color(0xffaaa383), Color(0xffb7b9ad)),
    rule = Color(0xffdfe3da), bar = Color(0xffe5e9e1), chipSelected = Color(0xffe9f2e9), surface = Color(0xfff8faf5), surfaceHover = Color(0xffedf2eb), softControl = Color(0xfff0f2eb), onAccent = Color.White,
)
private val DarkColors = KobakoColors(
    paper = Color(0xff1a1f1c), ink = Color(0xffe4e7e0), muted = Color(0xffa2ada5), accent = Color(0xff8cc4a6), accentStrong = Color(0xffa8d6bd),
    buttonPrimary = Color(0xff8cc4a6), buttonDanger = Color(0xffec948a), moneyNegative = Color(0xfff2978e), moneyPositive = Color(0xff8cc4a6), categoryDot = Color(0xff91a99d), categoryBar = Color(0xff6f8a7c),
    categoryDots = listOf(Color(0xff91a99d), Color(0xffb49a96), Color(0xffb0a784), Color(0xffb09eaf), Color(0xff98a6b0), Color(0xffa4b2a9), Color(0xffaaa383), Color(0xffb7b9ad)),
    rule = Color(0xff343d37), bar = Color(0xff2b332e), chipSelected = Color(0xff203227), surface = Color(0xff1f2521), surfaceHover = Color(0xff28302b), softControl = Color(0xff262d28), onAccent = Color(0xff142019),
)
private val LocalKobakoColors = staticCompositionLocalOf { LightColors }

object ThemeTokens {
    val colors: KobakoColors
        @Composable get() = LocalKobakoColors.current
}

fun categoryDotIndex(name: String): Int = when (name) {
    "住居" -> 0
    "食費" -> 1
    "日用品" -> 2
    "娯楽" -> 3
    "水道・光熱" -> 4
    "交通" -> 5
    "通信" -> 6
    "臨時収入" -> 7
    else -> (name.hashCode() and Int.MAX_VALUE) % 8
}

fun KobakoColors.categoryColor(name: String): Color = categoryDots[categoryDotIndex(name)]
val KobakoTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.Both),
)
private val KobakoTypography = Typography(
    displaySmall = KobakoTextStyle.copy(fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Medium),
    titleLarge = KobakoTextStyle.copy(fontSize = 21.sp, lineHeight = 24.15.sp, fontWeight = FontWeight.Medium),
    titleMedium = KobakoTextStyle.copy(fontSize = 15.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.Medium),
    titleSmall = KobakoTextStyle.copy(fontSize = 14.sp, lineHeight = 22.4.sp, fontWeight = FontWeight.Medium),
    bodyLarge = KobakoTextStyle.copy(fontSize = 16.sp, lineHeight = 18.4.sp),
    bodyMedium = KobakoTextStyle.copy(fontSize = 14.sp, lineHeight = 22.4.sp),
    bodySmall = KobakoTextStyle.copy(fontSize = 12.sp, lineHeight = 17.4.sp),
    labelLarge = KobakoTextStyle.copy(fontSize = 14.sp, lineHeight = 20.3.sp),
    labelMedium = KobakoTextStyle.copy(fontSize = 12.sp, lineHeight = 15.6.sp),
    labelSmall = KobakoTextStyle.copy(fontSize = 11.sp, lineHeight = 16.sp),
)

private val LightScheme = lightColorScheme(primary = LightColors.accent, onPrimary = LightColors.onAccent, background = LightColors.paper, onBackground = LightColors.ink, surface = LightColors.surface, onSurface = LightColors.ink, outline = LightColors.rule)
private val DarkScheme = darkColorScheme(primary = DarkColors.accent, onPrimary = DarkColors.onAccent, background = DarkColors.paper, onBackground = DarkColors.ink, surface = DarkColors.surface, onSurface = DarkColors.ink, outline = DarkColors.rule)

@Composable
fun KobakoTheme(theme: String, content: @Composable () -> Unit) {
    val dark = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(LocalKobakoColors provides if (dark) DarkColors else LightColors, LocalTextStyle provides KobakoTextStyle) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, typography = KobakoTypography, content = content)
    }
}

val PageHorizontalPadding = 16.dp
val SectionSpace = 24.dp
val ListBottomPadding = 188.dp
val FabSize = 56.dp
val FabContentGap = 24.dp
val FabContentEndPadding = PageHorizontalPadding + FabSize + FabContentGap
val FabBottomPadding = 16.dp
