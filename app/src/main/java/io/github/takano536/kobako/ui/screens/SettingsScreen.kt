package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.BuildConfig
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController, requestNotifications: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_settings)) }
        item { SettingsSectionTitle(stringResource(R.string.connection)) }
        item { SettingsRow(stringResource(R.string.server), state.settings.url.ifBlank { stringResource(R.string.not_connected) }, arrow = true) }
        item { SettingsSectionTitle(stringResource(R.string.appearance), topPadding = 32.dp) }
        item { ThemeChoices(state.settings.theme, viewModel::setTheme) }
        item { SettingsRow(stringResource(R.string.payment_notifications), trailing = { Switch(state.settings.notifications, { enabled -> viewModel.setNotifications(enabled); if (enabled) requestNotifications() }) }) }
        item { SettingsSectionTitle(stringResource(R.string.app_information), topPadding = 32.dp) }
        item { SettingsRow(stringResource(R.string.version), BuildConfig.VERSION_NAME) }
        item { SettingsRow(stringResource(R.string.license), "AGPL-3.0-only") }
        item { SettingsRow(stringResource(R.string.source_code), arrow = true, onClick = { uriHandler.openUri("https://github.com/takano536/kobako") }) }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, topPadding: androidx.compose.ui.unit.Dp = 32.dp) {
    Text(title, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 8.dp), color = ThemeTokens.colors.ink, fontSize = 15.sp, lineHeight = 19.5.sp, fontWeight = FontWeight.Medium)
}
@Composable
private fun SettingsRow(label: String, value: String? = null, arrow: Boolean = false, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val contents: @Composable () -> Unit = {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(label, color = ThemeTokens.colors.ink, modifier = Modifier.weight(1f), fontSize = 14.sp, lineHeight = 22.4.sp)
                if (value != null) Text(value, color = ThemeTokens.colors.muted, fontSize = 14.sp, lineHeight = 22.4.sp, maxLines = 1)
                trailing?.invoke()
                if (trailing == null) Box(Modifier.width(20.dp), contentAlignment = Alignment.CenterEnd) { if (arrow) Text("→", color = ThemeTokens.colors.muted, fontSize = 14.sp, lineHeight = 22.4.sp) }
            }
        }
    }
    if (onClick == null) contents() else Row(Modifier.fillMaxWidth().clickable(onClick = onClick)) { contents() }

}

@Composable
private fun ThemeChoices(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark)
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        choices.forEachIndexed { index, (key, label) ->
            if (index > 0) Spacer(Modifier.height(if (index == 1) 1.dp else 8.dp))
            val cardPadding = if (key == "system") PaddingValues(start = 8.dp, top = 5.dp, end = 8.dp, bottom = 11.dp) else PaddingValues(8.dp)
            Row(Modifier.fillMaxWidth().height(62.dp).border(1.dp, if (selected == key) ThemeTokens.colors.accent else ThemeTokens.colors.rule, MaterialTheme.shapes.medium).clickable { onSelect(key) }.padding(cardPadding), verticalAlignment = Alignment.CenterVertically) {
                ThemePreview(key, Modifier.width(88.dp).height(44.dp))
                Column(Modifier.padding(start = 8.dp), verticalArrangement = if (key == "system") Arrangement.spacedBy((-8).dp, Alignment.Top) else Arrangement.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioMark(selected == key)
                        Text(stringResource(label), color = ThemeTokens.colors.ink, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, lineHeight = 22.4.sp, fontWeight = if (selected == key) FontWeight.Medium else FontWeight.Normal)
                    }
                    if (key == "system") Text(stringResource(R.string.theme_system_hint), color = ThemeTokens.colors.muted, modifier = Modifier.padding(start = 28.dp), fontSize = 11.sp, lineHeight = 16.sp, maxLines = 1)
                }
            }
        }
    }
    Spacer(Modifier.height(7.dp))
}

@Composable
private fun RadioMark(selected: Boolean) {
    Box(Modifier.size(20.dp).border(2.dp, if (selected) ThemeTokens.colors.accent else ThemeTokens.colors.muted, androidx.compose.foundation.shape.CircleShape), contentAlignment = Alignment.Center) {
        if (selected) Box(Modifier.size(10.dp).background(ThemeTokens.colors.accent, androidx.compose.foundation.shape.CircleShape))
    }
}

@Composable
private fun ThemePreview(key: String, modifier: Modifier = Modifier) {
    val lightPaper = Color(0xfffcfcf8)
    val lightInk = Color(0xff292e2b)
    val lightMuted = Color(0xff68736c)
    val lightRule = Color(0xffdfe3da)
    val lightAccent = Color(0xff426d59)
    val darkPaper = Color(0xff1a1f1c)
    val darkInk = Color(0xffe4e7e0)
    val darkMuted = Color(0xffa2ada5)
    val darkRule = Color(0xff343d37)
    val darkAccent = Color(0xff8cc4a6)
    Row(modifier.border(1.dp, ThemeTokens.colors.rule, MaterialTheme.shapes.small)) {
        val sheets = if (key == "system") listOf(lightPaper to (lightInk to lightMuted), darkPaper to (darkInk to darkMuted)) else if (key == "light") listOf(lightPaper to (lightInk to lightMuted), lightPaper to (lightInk to lightMuted)) else listOf(darkPaper to (darkInk to darkMuted), darkPaper to (darkInk to darkMuted))
        sheets.forEachIndexed { index, (paper, textColors) ->
            val (ink, muted) = textColors
            Column(Modifier.weight(1f).fillMaxWidth().background(paper).padding(horizontal = 5.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(Modifier.fillMaxWidth(0.6f).height(4.dp).background(ink, androidx.compose.foundation.shape.CircleShape))
                Box(Modifier.fillMaxWidth().height(1.dp).background(if (paper == lightPaper) lightRule else darkRule))
                Box(Modifier.fillMaxWidth(0.85f).height(3.dp).background(muted, androidx.compose.foundation.shape.CircleShape))
                Box(Modifier.fillMaxWidth(0.35f).height(4.dp).background(if (paper == lightPaper) lightAccent else darkAccent, androidx.compose.foundation.shape.CircleShape))
            }
            if (index == 0) Spacer(Modifier.width(1.dp).background(if (paper == lightPaper) lightRule else darkRule))
        }
    }
}
