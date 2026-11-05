package io.github.takano536.kobako.ui.screens
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.theme.categoryColor

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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.platform.LocalConfiguration
import io.github.takano536.kobako.ui.theme.PageHorizontalPadding
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.domain.Transaction
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt
data class DemoHeaderState(
    val active: Boolean = false,
    val onExit: () -> Unit = {},
)

val LocalDemoHeaderState = staticCompositionLocalOf { DemoHeaderState() }

fun yen(value: Long): String = NumberFormat.getNumberInstance(Locale.JAPAN).format(kotlin.math.abs(value)) + "円"
fun categoryShareLabel(amount: Long, total: Long): String {
    val share = if (total > 0) amount.toDouble() / total else 0.0
    return if (share > 0 && share < 0.01) "<1%" else "${(share * 100).roundToInt()}%"
}
fun signedYen(value: Long): String = when { value < 0 -> "−${yen(value)}"; value > 0 -> "＋${yen(value)}"; else -> yen(value) }
@Composable
fun PageHeader(title: String, count: Int? = null) {
    val demo = LocalDemoHeaderState.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = PageHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                fontSize = 23.sp,
                lineHeight = 28.sp,
                letterSpacing = (-0.03).em,
                fontWeight = FontWeight.Medium,
                color = ThemeTokens.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            count?.let {
                Text(
                    "${it}件",
                    modifier = Modifier.padding(start = 8.dp),
                    color = ThemeTokens.colors.muted,
                    fontSize = 14.sp,
                )
            }
        }
        if (demo.active) {
            Row(
                Modifier
                    .height(44.dp)
                    .clickable(onClick = demo.onExit)
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("デモ", color = ThemeTokens.colors.muted, fontSize = 11.sp)
                Spacer(Modifier.width(8.dp))
                Text("終了", color = ThemeTokens.colors.accent, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding).padding(top = 35.dp).height(40.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = ThemeTokens.colors.ink)
        trailing?.let {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                it()
            }
        }
    }
}

@Composable
fun Metric(label: String, value: Long, color: Color = ThemeTokens.colors.ink, primary: Boolean = false, signed: Boolean = false) {
    Column {
        Text(
            label,
            color = if (primary) ThemeTokens.colors.ink else ThemeTokens.colors.muted,
            fontSize = if (primary) 14.sp else 11.sp,
            lineHeight = if (primary) 19.sp else 16.sp,
            fontWeight = if (primary) FontWeight.Medium else FontWeight.Normal,
            style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
        )
        Text(
            if (signed) signedYen(value) else yen(value),
            modifier = Modifier.height(if (primary) 30.dp else 21.dp),
            color = color,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (primary) 26.sp else 16.sp,
                lineHeight = if (primary) 30.sp else 21.sp,
                letterSpacing = if (primary) (-0.04).sp else 0.sp,
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum",
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}
@Composable
fun SummaryPanel(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    val ruleColor = ThemeTokens.colors.rule
    Column(
        Modifier.fillMaxWidth()
            .drawBehind {
                drawLine(ruleColor, Offset(0f, size.height - 1.dp.toPx()), Offset(size.width, size.height - 1.dp.toPx()), strokeWidth = 1.dp.toPx())
            }
            .padding(start = PageHorizontalPadding, top = 12.dp, end = PageHorizontalPadding, bottom = 12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom, content = content)
    }
}

@Composable
fun MonthSelector(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(44.dp).padding(start = PageHorizontalPadding), verticalAlignment = Alignment.CenterVertically) {
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
fun CategoryDot(name: String, modifier: Modifier = Modifier, color: Color? = null) { Box(Modifier.then(modifier).size(6.dp).clip(CircleShape).background(color ?: ThemeTokens.colors.categoryColor(name))) }

@Composable
fun CategoryRow(name: String, amount: Long, total: Long) {
    val share = if (total > 0) amount.toDouble() / total else 0.0
    val shareLabel = categoryShareLabel(amount, total)
    Column(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding)) {
        Row(Modifier.fillMaxWidth().height(28.dp), verticalAlignment = Alignment.CenterVertically) {
            CategoryDot(name)
            Text(name, Modifier.padding(start = 8.dp).weight(1f), fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(yen(amount), fontSize = 14.sp, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
        }
        Row(Modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.weight(1f).padding(top = 4.dp).height(2.dp).background(ThemeTokens.colors.bar)) { Box(Modifier.fillMaxWidth(share.toFloat().coerceIn(0f, 1f)).height(2.dp).background(ThemeTokens.colors.categoryBar)) }
            Text(shareLabel, Modifier.width(57.dp), textAlign = TextAlign.Start, color = ThemeTokens.colors.muted, fontSize = 12.sp)
        }
    }
}

@Composable fun transactionColor(amount: Long, type: String): Color = when { type == "transfer" || amount == 0L -> ThemeTokens.colors.ink; amount < 0 -> ThemeTokens.colors.moneyNegative; else -> ThemeTokens.colors.moneyPositive }

@Composable
fun TransactionGridRow(transaction: Transaction, date: String? = null, rowVerticalPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    val config = LocalConfiguration.current
    val categoryWidth = (config.screenWidthDp * 0.18f).dp.coerceIn(64.dp, 112.dp)
    val transfer = transaction.type == "transfer"
    Column(Modifier.fillMaxWidth().padding(horizontal = PageHorizontalPadding, vertical = rowVerticalPadding)) {
        date?.let { Text(it, color = ThemeTokens.colors.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.heightIn(min = 25.dp)) }
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.Top) {
            CategoryDot(transaction.category, Modifier.padding(top = 12.dp), if (transfer) ThemeTokens.colors.muted else null)
            Text(transaction.category, modifier = Modifier.width(categoryWidth).padding(start = 8.dp), color = if (transfer) ThemeTokens.colors.muted else ThemeTokens.colors.ink, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(transaction.account, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(transaction.memo.orEmpty(), modifier = Modifier.weight(1f, fill = false), color = ThemeTokens.colors.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (transaction.origin == "api") Text("API", Modifier.padding(start = 6.dp).background(ThemeTokens.colors.surfaceHover, RoundedCornerShape(50)).padding(horizontal = 5.dp, vertical = 1.dp), color = ThemeTokens.colors.accentStrong, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(if (transaction.type == "transfer") yen(transaction.amount) else signedYen(transaction.amount), color = transactionColor(transaction.amount, transaction.type), style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontFeatureSettings = "tnum"), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun RecentTransactionRow(date: String, category: String, account: String, memo: String?, amount: Long, type: String, origin: String = "local") {
    TransactionGridRow(Transaction(0L, java.time.LocalDate.now(), category, account, memo, amount, type, origin), date, rowVerticalPadding = 10.dp)
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit, hasChevron: Boolean = false, hasCheck: Boolean = false) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        Box(Modifier.height(36.dp).border(1.dp, if (selected) ThemeTokens.colors.accent.copy(alpha = 0.22f) else ThemeTokens.colors.rule, RoundedCornerShape(6.dp)).background(if (selected) ThemeTokens.colors.chipSelected else Color.Transparent, RoundedCornerShape(6.dp)).pointerInput(onClick) { detectTapGestures(onTap = { onClick() }) }.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (hasCheck) Text("✓", color = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted, fontSize = 12.sp)
                Text(label, color = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted, fontSize = 12.sp)
                if (hasChevron) Icon(painterResource(R.drawable.ic_action_chevron_down), null, modifier = Modifier.size(10.dp), tint = if (selected) ThemeTokens.colors.accentStrong else ThemeTokens.colors.muted)
            }
        }
    }
}
