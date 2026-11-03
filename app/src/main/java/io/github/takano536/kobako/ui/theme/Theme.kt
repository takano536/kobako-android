package io.github.takano536.kobako.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object ThemeTokens {
    val paper = Color(0xfffcfcf8); val ink = Color(0xff292e2b); val muted = Color(0xff68736c); val accent = Color(0xff426d59); val buttonPrimary = Color(0xff2f6b52); val buttonDanger = Color(0xffa84f45); val moneyNegative = Color(0xffa33a36); val moneyPositive = Color(0xff245c9c); val rule = Color(0xffdfe3da); val surface = Color(0xfff8faf5); val surfaceHover = Color(0xffedf2eb); val softControl = Color(0xfff0f2eb)
    val darkPaper = Color(0xff1a1f1c); val darkInk = Color(0xffe4e7e0); val darkMuted = Color(0xffa2ada5); val darkAccent = Color(0xff8cc4a6); val darkRule = Color(0xff343d37); val darkSurface = Color(0xff1f2521)
}
private val Light = lightColorScheme(primary = ThemeTokens.accent, onPrimary = Color.White, background = ThemeTokens.paper, onBackground = ThemeTokens.ink, surface = ThemeTokens.surface, onSurface = ThemeTokens.ink, outline = ThemeTokens.rule)
private val Dark = darkColorScheme(primary = ThemeTokens.darkAccent, onPrimary = ThemeTokens.darkPaper, background = ThemeTokens.darkPaper, onBackground = ThemeTokens.darkInk, surface = ThemeTokens.darkSurface, onSurface = ThemeTokens.darkInk, outline = ThemeTokens.darkRule)
@Composable fun KobakoTheme(theme: String, content: @Composable () -> Unit) { MaterialTheme(colorScheme = if (theme == "dark") Dark else Light, typography = Typography(), content = content) }
val SectionSpace = 32.dp
