package io.github.takano536.kobako.ui.screens

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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.BuildConfig
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.ListBottomPadding

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController, requestNotifications: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_settings), settingsActive = true) { } }
        item { SectionTitle(stringResource(R.string.connection)) }
        item {
            SettingsRow(label = stringResource(R.string.server), value = state.settings.url.ifBlank { stringResource(R.string.not_connected) }, arrow = true)
        }
        item { SectionTitle(stringResource(R.string.appearance)) }
        item { ThemeChoices(selected = state.settings.theme, onSelect = viewModel::setTheme) }
        item {
            SettingsRow(label = stringResource(R.string.payment_notifications), trailing = {
                Switch(checked = state.settings.notifications, onCheckedChange = { enabled -> viewModel.setNotifications(enabled); if (enabled) requestNotifications() }, modifier = Modifier.height(48.dp))
            })
        }
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
            Text(label, color = ThemeTokens.colors.ink, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (value != null) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { Text(value, color = ThemeTokens.colors.muted, maxLines = 1, style = MaterialTheme.typography.bodyMedium) }
            } else Spacer(Modifier.weight(1f))
            trailing?.invoke() ?: Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterEnd) {
                if (arrow) Icon(painterResource(R.drawable.ic_action_chevron_forward), contentDescription = null, tint = ThemeTokens.colors.muted)
            }
        }
    }
    if (onClick == null) contents() else TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { contents() }
    HorizontalDivider(color = ThemeTokens.colors.rule, thickness = 1.dp)
}

@Composable
private fun ThemeChoices(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark)
    Column(Modifier.fillMaxWidth()) {
        choices.forEach { (key, label) ->
            TextButton(onClick = { onSelect(key) }, modifier = Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == key, onClick = null)
                    Text(stringResource(label), color = ThemeTokens.colors.ink, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                    if (key == "system") {
                        Text(stringResource(R.string.theme_system_hint), color = ThemeTokens.colors.muted, modifier = Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.bodySmall)
                    } else Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
