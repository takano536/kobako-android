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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.BuildConfig
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun SettingsScreen(viewModel: AppViewModel, requestNotifications: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 152.dp)) {
        item { PageTitle(stringResource(R.string.nav_settings)) }
        item { SettingsSectionTitle(stringResource(R.string.connection)) }
        item {
            SettingsRow(
                label = stringResource(R.string.server),
                value = state.settings.url.ifBlank { stringResource(R.string.not_connected) },
                arrow = true,
                onClick = { /* Connection editing remains the onboarding flow in the read-only MVP. */ },
            )
        }
        item { SettingsSectionTitle(stringResource(R.string.appearance)) }
        item { ThemeChoices(selected = state.settings.theme, onSelect = viewModel::setTheme) }
        item {
            SettingsRow(
                label = stringResource(R.string.payment_notifications),
                value = null,
                trailing = {
                    Switch(
                        checked = state.settings.notifications,
                        onCheckedChange = { enabled ->
                            viewModel.setNotifications(enabled)
                            if (enabled) requestNotifications()
                        },
                        modifier = Modifier.height(48.dp),
                    )
                },
            )
        }
        item { SettingsSectionTitle(stringResource(R.string.app_information)) }
        item { SettingsRow(stringResource(R.string.version), BuildConfig.VERSION_NAME) }
        item { SettingsRow(stringResource(R.string.license), "AGPL-3.0-only") }
        item { SettingsRow(stringResource(R.string.source_code), arrow = true) }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 24.dp, bottom = 6.dp))
}
/** Values always reserve the same 28dp trailing slot as an arrow. */
@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    arrow: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val modifier = Modifier.fillMaxWidth().height(52.dp)
    if (onClick == null) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            SettingsContents(label, value, arrow, trailing)
        }
    } else {
        TextButton(onClick = onClick, modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SettingsContents(label, value, arrow, trailing)
            }
        }
    }
    HorizontalDivider(color = ThemeTokens.colors.rule)
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SettingsContents(
    label: String,
    value: String?,
    arrow: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    Text(label, color = ThemeTokens.colors.ink, modifier = Modifier.weight(1f))
    if (value != null) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text(value, color = ThemeTokens.colors.muted, maxLines = 1)
        }
    } else {
        Spacer(Modifier.weight(1f))
    }
    if (trailing != null) trailing()
    else Box(Modifier.width(28.dp), contentAlignment = Alignment.CenterEnd) {
        if (arrow) Text("›", color = ThemeTokens.colors.muted, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ThemeChoices(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf(
        "system" to R.string.theme_system,
        "light" to R.string.theme_light,
        "dark" to R.string.theme_dark,
    )
    Column(Modifier.fillMaxWidth()) {
        choices.forEach { (key, label) ->
            TextButton(
                onClick = { onSelect(key) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selected == key) "●" else "○", color = ThemeTokens.colors.accent, modifier = Modifier.width(32.dp))
                    Text(stringResource(label), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
