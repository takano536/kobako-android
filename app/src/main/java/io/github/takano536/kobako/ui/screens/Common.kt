package io.github.takano536.kobako.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.categoryColor
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.PageHorizontalPadding
import io.github.takano536.kobako.ui.theme.FabContentEndPadding
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun yen(value: Long): String = NumberFormat.getNumberInstance(Locale.JAPAN).format(kotlin.math.abs(value)) + "円"
fun signedYen(value: Long): String = when {
    value < 0 -> "−${yen(value)}"
    value > 0 -> "＋${yen(value)}"
    else -> yen(value)
}

@Composable
fun PageHeader(title: String, settingsActive: Boolean = false, onSettings: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = ThemeTokens.colors.ink)
        Spacer(Modifier.weight(1f))
        if (onSettings != null) {
            IconButton(onClick = onSettings, modifier = Modifier.size(44.dp)) {
                Icon(painterResource(R.drawable.ic_nav_settings), contentDescription = stringResource(R.string.nav_settings), tint = if (settingsActive) ThemeTokens.colors.accent else ThemeTokens.colors.muted)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding).padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = ThemeTokens.colors.ink)
        trailing?.invoke()
    }
}

@Composable
fun Metric(label: String, value: Long, color: Color = ThemeTokens.colors.ink, primary: Boolean = false) {
    Column {
        Text(label, color = ThemeTokens.colors.muted, style = if (primary) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall)
        Text(
            yen(value),
            color = color,
            style = (if (primary) MaterialTheme.typography.displaySmall else MaterialTheme.typography.titleLarge).copy(fontFeatureSettings = "tnum"),
            fontWeight = if (primary) FontWeight.Medium else FontWeight.Normal,
        )
    }
}

@Composable
fun MonthSelector(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_action_chevron_left), contentDescription = stringResource(R.string.previous_month), tint = ThemeTokens.colors.ink)
        }
        Text(
            month.format(DateTimeFormatter.ofPattern("yyyy年M月")),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
            Icon(painterResource(R.drawable.ic_action_chevron_right), contentDescription = stringResource(R.string.next_month), tint = ThemeTokens.colors.ink)
        }
    }
}

@Composable
fun EmptyMessage(message: String) {
    Text(message, color = ThemeTokens.colors.muted, modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 24.dp), style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun ReadOnlyFab(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.padding(end = PageHorizontalPadding, bottom = 64.dp),
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(ThemeTokens.colors.buttonPrimary),
        ) {
            Icon(painterResource(R.drawable.ic_action_add), contentDescription = stringResource(R.string.register_transaction), tint = ThemeTokens.colors.onAccent)
        }
    }
}

@Composable
fun CategoryDot(name: String, modifier: Modifier = Modifier) {
    Box(Modifier.then(modifier).size(8.dp).clip(CircleShape).background(ThemeTokens.colors.categoryColor(name)))
}

@Composable
fun CategoryRow(name: String, amount: Long, total: Long) {
    val share = if (total > 0) amount.toDouble() / total else 0.0
    val shareLabel = if (share > 0 && share < 0.005) "<1%" else "${(share * 100).roundToInt()}%"
    Column(Modifier.fillMaxWidth().padding(start = PageHorizontalPadding, end = FabContentEndPadding, top = 7.dp, bottom = 7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(name)
            Text(name, modifier = Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(yen(amount), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
            Text(shareLabel, modifier = Modifier.width(44.dp), textAlign = TextAlign.End, color = ThemeTokens.colors.muted, style = MaterialTheme.typography.bodySmall)
        }
        Row(Modifier.padding(start = 16.dp, top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(4.dp).clip(CircleShape).background(ThemeTokens.colors.bar)) {
                Box(Modifier.fillMaxWidth(share.toFloat().coerceIn(0f, 1f)).height(4.dp).clip(CircleShape).background(ThemeTokens.colors.categoryBar))
            }
            Spacer(Modifier.width(44.dp))
        }
    }
}

@Composable
fun transactionColor(amount: Long, type: String): Color = when {
    type == "transfer" || amount == 0L -> ThemeTokens.colors.ink
    amount < 0 -> ThemeTokens.colors.moneyNegative
    else -> ThemeTokens.colors.moneyPositive
}

@Composable
fun RecentTransactionRow(date: String, category: String, account: String, memo: String?, amount: Long, type: String) {
    Column(Modifier.fillMaxWidth().padding(start = PageHorizontalPadding, end = FabContentEndPadding, top = 8.dp, bottom = 8.dp)) {
        Text(date, color = ThemeTokens.colors.muted, style = MaterialTheme.typography.labelSmall)
        Row(Modifier.fillMaxWidth().padding(top = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(category)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(category, style = MaterialTheme.typography.bodyMedium)
                Text(listOfNotNull(account, memo).joinToString("・"), color = ThemeTokens.colors.muted, style = MaterialTheme.typography.bodySmall)
            }
            Text(signedYen(amount), color = transactionColor(amount, type), style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.height(40.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)) {
        Text(label, color = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
    }
}
