package io.github.takano536.kobako.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.Transaction
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import io.github.takano536.kobako.ui.theme.ThemeTokens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(viewModel: AppViewModel, nav: NavHostController, snackbarHostState: SnackbarHostState) {
    val repository = viewModel.repository()
    val month by viewModel.monthFlow.collectAsStateWithLifecycle()
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf<String?>(null) }
    var account by remember { mutableStateOf<String?>(null) }
    var apiOnly by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(repository, month) { failed = false; transactions = emptyList(); repository?.transactions(month.toString())?.onSuccess { transactions = it }?.onFailure { failed = true } }
    val filtered = transactions.filter { (type == null || it.type == type) && (category == null || it.category == category) && (account == null || it.account == account) && (!apiOnly || it.origin == "api") }
    val types = listOf(null to "種別") + transactions.map { it.type to when (it.type) { "expense" -> "支出"; "income" -> "収入"; else -> "振替" } }.distinctBy { it.first }
    val categories = listOf(null to "カテゴリ") + transactions.map { it.category }.distinct().map { it to it }
    val accounts = listOf(null to "資産") + transactions.map { it.account }.distinct().map { it to it }
    val readonlyMessage = stringResource(R.string.readonly_notice)
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
            item { PageHeader(stringResource(R.string.nav_transactions), transactions.size) }
            item { MonthSelector(month, { viewModel.moveMonth(-1) }, { viewModel.moveMonth(1) }) }
            item {
                Column {
                    Row(Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionMenuChip(types, type, { type = it })
                        TransactionMenuChip(categories, category, { category = it })
                        TransactionMenuChip(accounts, account, { account = it })
                        FilterChip("APIのみ", apiOnly, { apiOnly = !apiOnly }, hasCheck = true)
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }
            when {
                failed -> item { EmptyMessage(stringResource(R.string.read_error)) }
                filtered.isEmpty() -> item { EmptyMessage(stringResource(R.string.no_transactions)) }
                else -> filtered.groupBy { it.occurredOn }.toSortedMap(compareByDescending { it }).entries.forEachIndexed { index, (date, rows) ->
                    item(key = "date-$date") { DateHeader(date, index > 0) }
                    items(rows, key = { "tx-${it.id}" }) { TransactionGridRow(it) }
                }
            }
        }
        ReadOnlyFab(true, onClick = { scope.launch { snackbarHostState.showSnackbar(readonlyMessage) } }, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun <T> TransactionMenuChip(options: List<Pair<T?, String>>, selected: T?, onSelected: (T?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.height(36.dp)) {
        FilterChip(options.first { it.first == selected }.second, selected != null, { expanded = true }, hasChevron = true)
        DropdownMenu(expanded, { expanded = false }) { options.forEach { (value, label) -> DropdownMenuItem({ Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }, { onSelected(value); expanded = false }) } }
    }
}


@Composable
private fun DateHeader(date: LocalDate, spaced: Boolean) {
    Text(date.format(DateTimeFormatter.ofPattern("M月d日（E）", Locale.JAPANESE)), color = ThemeTokens.colors.muted, fontSize = 12.sp, lineHeight = 17.4.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = if (spaced) 16.dp else 4.dp).heightIn(min = 25.dp))
}
