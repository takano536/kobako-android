package io.github.takano536.kobako.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.PageHorizontalPadding
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.categoryColor
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

fun yen(value: Long): String = NumberFormat.getNumberInstance(Locale.JAPAN).format(kotlin.math.abs(value)) + "円"
fun categoryShareLabel(amount: Long, total: Long): String {
    val share = if (total > 0) amount.toDouble() / total else 0.0
    return if (share > 0 && share < 0.01) "<1%" else "${(share * 100).roundToInt()}%"
}
fun signedYen(value: Long): String = when { value < 0 -> "−${yen(value)}"; value > 0 -> "＋${yen(value)}"; else -> yen(value) }

@Composable
fun PageHeader(title: String, count: Int? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 23.sp, lineHeight = 28.sp, letterSpacing = (-0.03).em, fontWeight = FontWeight.Medium, color = ThemeTokens.colors.ink)
        count?.let { Text("${it}件", modifier = Modifier.padding(start = 8.dp), color = ThemeTokens.colors.muted, fontSize = 14.sp) }
    }
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding).padding(top = 32.dp).heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = ThemeTokens.colors.ink)
        trailing?.invoke()
    }
}

@Composable
fun Metric(label: String, value: Long, color: Color = ThemeTokens.colors.ink, primary: Boolean = false) {
    Column {
        Text(label, color = if (primary) ThemeTokens.colors.ink else ThemeTokens.colors.muted, fontSize = if (primary) 14.sp else 11.sp, fontWeight = if (primary) FontWeight.Medium else FontWeight.Normal)
        Text(yen(value), color = color, style = MaterialTheme.typography.bodyLarge.copy(fontSize = if (primary) 26.sp else 16.sp, lineHeight = if (primary) 31.sp else 21.sp, letterSpacing = if (primary) (-0.04).sp else 0.sp, fontWeight = if (primary) FontWeight.SemiBold else FontWeight.Medium, fontFeatureSettings = "tnum"))
    }
}

@Composable
fun MonthSelector(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = PageHorizontalPadding).padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev, modifier = Modifier.offset(x = (-22).dp).size(44.dp)) { Icon(painterResource(R.drawable.ic_action_chevron_left), stringResource(R.string.previous_month), tint = ThemeTokens.colors.muted, modifier = Modifier.size(16.dp)) }
        Text(month.format(DateTimeFormatter.ofPattern("yyyy年M月")), modifier = Modifier.widthIn(min = 44.dp), textAlign = TextAlign.Center, fontSize = 12.sp, color = ThemeTokens.colors.ink, maxLines = 1)
        IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) { Icon(painterResource(R.drawable.ic_action_chevron_right), stringResource(R.string.next_month), tint = ThemeTokens.colors.muted, modifier = Modifier.size(16.dp)) }
    }
}

@Composable fun EmptyMessage(message: String) { Text(message, color = ThemeTokens.colors.muted, modifier = Modifier.padding(horizontal = PageHorizontalPadding, vertical = 24.dp), fontSize = 14.sp) }

@Composable
fun ReadOnlyFab(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible, modifier = modifier.padding(end = PageHorizontalPadding, bottom = 16.dp), enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
        IconButton(onClick, Modifier.size(56.dp).clip(CircleShape).background(ThemeTokens.colors.buttonPrimary)) { Icon(painterResource(R.drawable.ic_action_add), stringResource(R.string.register_transaction), tint = ThemeTokens.colors.onAccent) }
    }
}

@Composable
fun CategoryDot(name: String, modifier: Modifier = Modifier) { Box(Modifier.then(modifier).size(6.dp).clip(CircleShape).background(ThemeTokens.colors.categoryColor(name))) }

@Composable
fun CategoryRow(name: String, amount: Long, total: Long) {
    val share = if (total > 0) amount.toDouble() / total else 0.0
    val shareLabel = categoryShareLabel(amount, total)
    Column(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(name)
            Text(name, Modifier.padding(start = 8.dp).weight(1f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(yen(amount), fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
        }
        Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(2.dp).background(ThemeTokens.colors.bar)) { Box(Modifier.fillMaxWidth(share.toFloat().coerceIn(0f, 1f)).height(2.dp).background(ThemeTokens.colors.categoryBar)) }
            Text(shareLabel, Modifier.width(44.dp), textAlign = TextAlign.End, color = ThemeTokens.colors.muted, fontSize = 12.sp)
        }
    }
}

@Composable fun transactionColor(amount: Long, type: String): Color = when { type == "transfer" || amount == 0L -> ThemeTokens.colors.ink; amount < 0 -> ThemeTokens.colors.moneyNegative; else -> ThemeTokens.colors.moneyPositive }

@Composable
fun RecentTransactionRow(date: String, category: String, account: String, memo: String?, amount: Long, type: String, origin: String = "local") {
    Column(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding, vertical = 8.dp)) {
        Text(date, color = ThemeTokens.colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.height(25.dp))
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(category)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(category, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(listOfNotNull(account, memo).joinToString("・"), color = ThemeTokens.colors.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (origin == "api") Text("API", Modifier.padding(start = 6.dp).background(ThemeTokens.colors.surfaceHover, RoundedCornerShape(50)).padding(horizontal = 5.dp, vertical = 1.dp), color = ThemeTokens.colors.accentStrong, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(signedYen(amount), color = transactionColor(amount, type), fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit, hasChevron: Boolean = false) {
    TextButton(onClick, Modifier.height(36.dp).border(1.dp, if (selected) ThemeTokens.colors.accent.copy(alpha = 0.22f) else ThemeTokens.colors.rule, RoundedCornerShape(6.dp)).background(if (selected) ThemeTokens.colors.chipSelected else Color.Transparent, RoundedCornerShape(6.dp)), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted, fontSize = 12.sp)
            if (hasChevron) Icon(painterResource(R.drawable.ic_action_chevron_down), null, modifier = Modifier.size(10.dp), tint = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted)
        }
    }
}
