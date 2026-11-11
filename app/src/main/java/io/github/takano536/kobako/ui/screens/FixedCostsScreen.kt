package io.github.takano536.kobako.ui.screens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.FixedCost
import io.github.takano536.kobako.domain.FixedCosts
import io.github.takano536.kobako.domain.FixedCostsResult
import io.github.takano536.kobako.domain.scheduleLabel
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import io.github.takano536.kobako.ui.theme.KobakoTextStyle
import io.github.takano536.kobako.ui.theme.KobakoText

private val fixedGroupOptions = listOf("category" to "カテゴリ別", "frequency" to "頻度別", "account" to "資産別", "day" to "日付別", "none" to "なし")

private fun frequencyKey(item: FixedCost) = "${item.intervalUnit}-${item.intervalCount}"

private fun frequencyLabel(key: String): String {
    val unit = key.substringBefore('-')
    val count = key.substringAfter('-').toIntOrNull() ?: 1
    return if (unit == "year") "${count}年ごと" else if (count == 1) "毎月" else "${count}か月ごと"
}

private fun groupKeyOf(item: FixedCost, group: String): String = when (group) {
    "frequency" -> frequencyLabel(frequencyKey(item))
    "account" -> item.accountName
    "day" -> "${item.day}日"
    else -> item.categoryName ?: "未分類"
}

@Composable
fun FixedCostsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val repository = viewModel.repository()
    var data by remember { mutableStateOf<FixedCosts?>(null) }
    var unsupported by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf<String?>(null) }
    var frequency by remember { mutableStateOf<String?>(null) }
    var account by remember { mutableStateOf<String?>(null) }
    var group by remember { mutableStateOf("category") }
    LaunchedEffect(repository) {
        data = null
        unsupported = false
        failed = false
        when (val result = repository?.fixedCosts()) {
            is FixedCostsResult.Success -> data = result.value
            FixedCostsResult.Unsupported -> unsupported = true
            is FixedCostsResult.Failure -> failed = true
            null -> Unit
        }
    }
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_fixed_costs)) }
        when {
            unsupported -> item { EmptyMessage(stringResource(R.string.fixed_cost_api_unsupported)) }
            failed -> item { EmptyMessage(stringResource(R.string.read_error)) }
            data != null -> {
                val fixed = requireNotNull(data)
                val categories = fixed.items.mapNotNull { it.categoryName }.distinct()
                val accounts = fixed.items.map { it.accountName }.distinct()
                val frequencies = (fixed.items.map { frequencyKey(it) } + listOf("month-1", "year-1")).distinct()
                val filtered = fixed.items.filter {
                    (category == null || it.categoryName == category) &&
                        (account == null || it.accountName == account) &&
                        (frequency == null || frequencyKey(it) == frequency)
                }
                item {
                    SummaryPanel {
                        Metric(stringResource(R.string.fixed_monthly), fixed.monthly, ThemeTokens.colors.moneyNegative, primary = true)
                        Metric(stringResource(R.string.annualized), fixed.annual, ThemeTokens.colors.moneyNegative)
                    }
                }
                item {
                    FilterBar {
                        FixedCostMenuChip("並び順", fixedGroupOptions, group, { group = it })
                        FixedCostMenuChip("カテゴリ", categories.map { it to it }, category, { category = it })
                        FixedCostMenuChip("頻度", frequencies.map { it to frequencyLabel(it) }, frequency, { frequency = it })
                        FixedCostMenuChip("資産", accounts.map { it to it }, account, { account = it })
                        if (category != null || frequency != null || account != null) {
                            FilterClear { category = null; frequency = null; account = null }
                        }
                    }
                }
                when {
                    fixed.items.isEmpty() -> item { EmptyMessage(stringResource(R.string.no_fixed_costs)) }
                    filtered.isEmpty() -> item { EmptyMessage("条件に合う固定費がありません") }
                    group == "none" -> {
                        item { SectionTitle(stringResource(R.string.fixed_breakdown)) }
                        items(filtered, key = { it.id }) { FixedCostRow(it) }
                    }
                    else -> {
                        filtered.groupBy { groupKeyOf(it, group) }.toSortedMap().entries.forEachIndexed { index, (name, rows) ->
                            item(key = "group-$name") { GroupHeading(name, first = index == 0) }
                            items(rows, key = { it.id }) { FixedCostRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeading(name: String, first: Boolean) {
    val colors = ThemeTokens.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = if (first) 8.dp else 20.dp, bottom = 4.dp).heightIn(min = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, color = colors.muted, style = KobakoText.meta, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("金額", color = colors.muted, style = KobakoText.micro)
    }
}

@Composable
private fun <T> FixedCostMenuChip(label: String, options: List<Pair<T, String>>, selected: T?, onSelected: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val value = options.firstOrNull { it.first == selected }?.second
    Box {
        FilterChip(label, selected != null, { expanded = true }, value = value, hasChevron = true)
        DropdownMenu(expanded, { expanded = false }) {
            options.forEach { (option, text) ->
                DropdownMenuItem({ Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) }, { onSelected(option); expanded = false })
            }
        }
    }
}

@Composable
private fun FixedCostRow(item: FixedCost) {
    val colors = ThemeTokens.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.name, style = KobakoText.body)
                Text("${item.accountName}・${item.scheduleLabel()}", color = colors.muted, style = KobakoText.small)
            }
            Text(yen(item.amount), color = colors.moneyNegative, style = KobakoText.body.copy(fontFeatureSettings = "tnum"))
        }
        HorizontalDivider(color = colors.rule, thickness = 1.dp)
    }
}
