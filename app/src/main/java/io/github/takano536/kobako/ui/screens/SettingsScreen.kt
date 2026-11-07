package io.github.takano536.kobako.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.BuildConfig
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.KobakoText
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import io.github.takano536.kobako.ui.theme.PageHorizontalPadding
import io.github.takano536.kobako.ui.theme.Space
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun SettingsScreen(viewModel: AppViewModel, nav: NavHostController, requestNotifications: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_settings)) }
        item { SettingsSectionTitle(stringResource(R.string.connection)) }
        item { SettingsRow(stringResource(R.string.server), state.settings.url.ifBlank { stringResource(R.string.not_connected) }, arrow = true) }
        item { SettingsSectionTitle(stringResource(R.string.appearance)) }
        item { ThemeChoices(state.settings.theme, viewModel::setTheme) }
        item { SettingsRow(stringResource(R.string.payment_notifications), trailing = { KobakoSwitch(state.settings.notifications, { enabled -> viewModel.setNotifications(enabled); if (enabled) requestNotifications() }) }) }
        item { SettingsSectionTitle(stringResource(R.string.app_information)) }
        item { SettingsRow(stringResource(R.string.version), BuildConfig.VERSION_NAME) }
        item { SettingsRow(stringResource(R.string.license), "AGPL-3.0-only") }
        item { SettingsRow(stringResource(R.string.source_code), arrow = true, onClick = { uriHandler.openUri("https://github.com/takano536/kobako") }) }
    }
}

/** `.settings-section h2` — `--text-heading` above a 32px section gap. */
@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.fillMaxWidth().padding(start = PageHorizontalPadding, end = PageHorizontalPadding, top = Space.section, bottom = Space.item),
        color = ThemeTokens.colors.ink,
        style = KobakoText.heading,
    )
}

/**
 * `.settings-row`: 44px minimum, label on the left, value right-aligned, and a
 * fixed trailing slot. Rows without an arrow pad the value by 1.75rem so every
 * value shares one right edge.
 */
@Composable
private fun SettingsRow(
    label: String,
    value: String? = null,
    arrow: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = ThemeTokens.colors
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .heightIn(min = 44.dp)
                .padding(horizontal = PageHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = colors.ink, style = KobakoText.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f).width(Space.item))
            if (value != null) Text(value, color = colors.muted, style = KobakoText.body, maxLines = 1, overflow = TextOverflow.Ellipsis)
            when {
                trailing != null -> {
                    Spacer(Modifier.width(Space.item))
                    trailing()
                }
                arrow -> Box(Modifier.width(20.dp), contentAlignment = Alignment.CenterEnd) { Text("→", color = colors.muted, style = KobakoText.body) }
                value != null -> Spacer(Modifier.width(28.dp))
            }
        }
    }
}

/** `.theme-options` — three radio cards, each with a preview of two theme sheets. */
@Composable
private fun ThemeChoices(selected: String, onSelect: (String) -> Unit) {
    val choices = listOf("system" to R.string.theme_system, "light" to R.string.theme_light, "dark" to R.string.theme_dark)
    Column(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding)) {
        choices.forEachIndexed { index, (key, label) ->
            if (index > 0) Spacer(Modifier.height(Space.item))
            ThemeOptionCard(key, stringResource(label), if (key == "system") stringResource(R.string.theme_system_hint) else null, selected == key) { onSelect(key) }
        }
        // `.settings-section` is a grid with `gap: var(--space-item)`; the notification
        // row below the cards keeps exactly one 8px gap.
        Spacer(Modifier.height(Space.item))
    }
}

/** `.theme-option`: 8px padding, 1px border, 8px radius, 44px preview plus the radio text column. */
@Composable
private fun ThemeOptionCard(key: String, label: String, hint: String?, selected: Boolean, onClick: () -> Unit) {
    val colors = ThemeTokens.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.paper)
            .border(1.dp, if (selected) colors.accent else colors.rule, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 44.dp)
            .padding(Space.item),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ThemePreview(key, Modifier.width(88.dp).height(44.dp))
        Spacer(Modifier.width(Space.item))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioMark(selected)
                Spacer(Modifier.width(Space.item))
                Text(label, color = colors.ink, style = KobakoText.label, maxLines = 1)
            }
            if (hint != null) {
                Text(
                    hint,
                    modifier = Modifier.padding(start = 16.dp + Space.item),
                    color = colors.muted,
                    style = KobakoText.small,
                    maxLines = 1,
                )
            }
        }
    }
}

/** `.theme-option-indicator`: a 1rem ring, filled to a 40% radius when checked. */
@Composable
private fun RadioMark(selected: Boolean) {
    val colors = ThemeTokens.colors
    Box(
        Modifier.size(16.dp).clip(CircleShape).border(1.5.dp, if (selected) colors.accent else colors.muted, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) Box(Modifier.size(12.8.dp).background(colors.accent, CircleShape))
    }
}

/** `.theme-preview`: two 44px sheets of the theme, drawn from the palette itself. */
@Composable
private fun ThemePreview(key: String, modifier: Modifier = Modifier) {
    val light = PreviewPalette(Color(0xfffcfcf8), Color(0xff292e2b), Color(0xff68736c), Color(0xffdfe3da), Color(0xff426d59))
    val dark = PreviewPalette(Color(0xff1a1f1c), Color(0xffe4e7e0), Color(0xffa2ada5), Color(0xff343d37), Color(0xff8cc4a6))
    val sheets = when (key) {
        "light" -> listOf(light, light)
        "dark" -> listOf(dark, dark)
        else -> listOf(light, dark)
    }
    Row(modifier.clip(RoundedCornerShape(6.dp)).border(1.dp, ThemeTokens.colors.rule, RoundedCornerShape(6.dp))) {
        sheets.forEachIndexed { index, sheet ->
            if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(sheets[index - 1].rule))
            Column(
                Modifier.weight(1f).fillMaxHeight().background(sheet.paper).padding(horizontal = Space.related),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.fillMaxWidth(0.6f).height(6.dp).background(sheet.ink, CircleShape))
                Spacer(Modifier.height(Space.within))
                Box(Modifier.fillMaxWidth().height(1.dp).background(sheet.rule))
                Spacer(Modifier.height(Space.within))
                Box(Modifier.fillMaxWidth(0.85f).height(4.dp).background(sheet.muted, CircleShape))
                Spacer(Modifier.height(Space.within))
                Box(Modifier.fillMaxWidth(0.35f).height(6.dp).background(sheet.accent, CircleShape))
            }
        }
    }
}

private data class PreviewPalette(val paper: Color, val ink: Color, val muted: Color, val rule: Color, val accent: Color)

/** `.switch`: a 40x24 track with an 18px thumb inset by 3px. */
@Composable
private fun KobakoSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val colors = ThemeTokens.colors
    val thumb by animateDpAsState(if (checked) 19.dp else 3.dp, label = "thumb")
    Box(
        Modifier
            .width(40.dp)
            .height(24.dp)
            .clip(CircleShape)
            .background(if (checked) colors.buttonPrimary else colors.bar)
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(Modifier.align(Alignment.CenterStart).padding(start = thumb).size(18.dp).background(colors.paper, CircleShape))
    }
}
