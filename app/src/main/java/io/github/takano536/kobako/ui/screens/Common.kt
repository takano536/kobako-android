package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.takano536.kobako.ui.theme.ThemeTokens
import java.text.NumberFormat
import java.util.Locale

fun yen(value: Long): String = NumberFormat.getNumberInstance(Locale.JAPAN).format(value) + "円"
fun signedYen(value: Long): String = if (value >= 0) "+${yen(value)}" else "-${yen(-value)}"


@Composable
fun PageTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(vertical = 16.dp),
        color = ThemeTokens.colors.ink,
    )
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        trailing?.invoke()
    }
}

@Composable
fun Metric(label: String, value: Long, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(label, color = ThemeTokens.colors.muted, style = MaterialTheme.typography.bodySmall)
        Text(yen(value), color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MonthSelector(
    month: java.time.YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.IconButton(onClick = onPrev, modifier = Modifier.width(48.dp).height(48.dp)) {
            Text("‹", style = MaterialTheme.typography.headlineSmall, color = ThemeTokens.colors.muted)
        }
        Text(
            month.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年M月")),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.IconButton(onClick = onNext, modifier = Modifier.width(48.dp).height(48.dp)) {
            Text("›", style = MaterialTheme.typography.headlineSmall, color = ThemeTokens.colors.muted)
        }
    }
}

@Composable
fun EmptyMessage(message: String) {
    Text(message, color = ThemeTokens.colors.muted, modifier = Modifier.padding(vertical = 16.dp))
}

@Composable
fun RegisterFab(visible: Boolean, modifier: Modifier = Modifier) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier.padding(end = 16.dp, bottom = 8.dp),
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it },
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it },
    ) {
        androidx.compose.material3.FloatingActionButton(
            onClick = { /* Read-only MVP: registration is intentionally not implemented. */ },
            containerColor = ThemeTokens.colors.accent,
            contentColor = ThemeTokens.colors.onAccent,
            modifier = Modifier.width(56.dp).height(56.dp),
        ) { Text("+", style = MaterialTheme.typography.headlineSmall) }
    }
}
