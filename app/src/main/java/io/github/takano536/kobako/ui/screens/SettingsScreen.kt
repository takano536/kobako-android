package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_settings)) }
        item { SectionTitle(stringResource(R.string.connection)) }
        item { SettingsRow(stringResource(R.string.server), state.settings.url.ifBlank { stringResource(R.string.not_connected) }, arrow = true) }
        item { SectionTitle(stringResource(R.string.appearance)) }
        item { ThemeChoices(state.settings.theme, viewModel::setTheme) }
        item { SettingsRow(stringResource(R.string.payment_notifications), trailing = { Switch(state.settings.notifications, { enabled -> viewModel.setNotifications(enabled); if (enabled) requestNotifications() }) }) }
        item { SectionTitle(stringResource(R.string.app_information)) }
        item { SettingsRow(stringResource(R.string.version), BuildConfig.VERSION_NAME) }
        item { SettingsRow(stringResource(R.string.license), "AGPL-3.0-only") }
        item { SettingsRow(stringResource(R.string.source_code), arrow = true, onClick = { uriHandler.openUri("https://github.com/takano536/kobako") }) }
    }
}

@Composable
private fun SettingsRow(label: String, value: String? = null, arrow: Boolean = false, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    val contents: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = ThemeTokens.colors.ink, modifier = Modifier.weight(1f), fontSize = 14.sp)
            if (value != null) Text(value, color = ThemeTokens.colors.muted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
            else Spacer(Modifier.weight(1f))
            trailing?.invoke() ?: Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterEnd) { if (arrow) Icon(painterResource(R.drawable.ic_action_chevron_forward), null, tint = ThemeTokens.colors.muted) }
        }
    }
    if (onClick == null) contents() else TextButton(onClick, Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { contents() }
    HorizontalDivider(color = ThemeTokens.colors.rule, thickness = 1.dp)
}

@Composable
private fun ThemeChoices(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark)
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { (key, label) ->
            TextButton(onClick = { onSelect(key) }, modifier = Modifier.weight(1f).border(1.dp, if (selected == key) ThemeTokens.colors.accent else ThemeTokens.colors.rule, MaterialTheme.shapes.small), contentPadding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().height(46.dp).background(if (key == "dark") Color(0xff1a1f1c) else Color(0xfffcfcf8), MaterialTheme.shapes.extraSmall)) { if (selected == key) Box(Modifier.padding(6.dp).background(ThemeTokens.colors.accent, MaterialTheme.shapes.extraSmall).height(4.dp).fillMaxWidth()) }
                    Text(stringResource(label), color = ThemeTokens.colors.ink, fontSize = 12.sp, fontWeight = if (selected == key) FontWeight.Medium else FontWeight.Normal)
                    if (key == "system") Text(stringResource(R.string.theme_system_hint), color = ThemeTokens.colors.muted, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}
