package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.Transaction
import io.github.takano536.kobako.ui.Routes
import io.github.takano536.kobako.ui.theme.FabContentEndPadding
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import io.github.takano536.kobako.ui.theme.PageHorizontalPadding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(viewModel: AppViewModel, nav: NavHostController, snackbarHostState: SnackbarHostState) {
    val repository = viewModel.repository()
    val month by viewModel.monthFlow.collectAsStateWithLifecycle()
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    var filter by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val readonlyMessage = stringResource(R.string.readonly_notice)
    LaunchedEffect(repository, month) {
        transactions = emptyList()
        failed = false
        repository?.transactions(month.toString())?.onSuccess { transactions = it }?.onFailure { failed = true }
    }
    LaunchedEffect(listState) {
        var previous = 0
        snapshotFlow { listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged().collectLatest { position ->
                fabVisible = position <= 8 || position < previous
                previous = position
            }
    }
    val visibleTransactions = transactions.filter { tx -> filter == 0 || (filter == 1 && tx.amount < 0) || (filter == 2 && tx.type == "income") }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
            item {
                PageHeader(stringResource(R.string.nav_transactions)) { nav.navigate(Routes.settings) }
                MonthSelector(month, { viewModel.moveMonth(-1) }, { viewModel.moveMonth(1) })
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(stringResource(R.string.filter_all), filter == 0) { filter = 0 }
                    FilterChip(stringResource(R.string.filter_expense), filter == 1) { filter = 1 }
                    FilterChip(stringResource(R.string.filter_income), filter == 2) { filter = 2 }
                }
            }
            if (failed) item { EmptyMessage(stringResource(R.string.read_error)) }
            else if (visibleTransactions.isEmpty()) item { EmptyMessage(stringResource(R.string.no_transactions)) }
            visibleTransactions.groupBy { it.occurredOn }.toSortedMap(reverseOrder()).forEach { (date, rows) ->
                item(key = "date-$date") { DateHeader(date) }
                items(rows, key = { it.id }) { TransactionRow(it) }
            }
        }
        ReadOnlyFab(fabVisible, onClick = { scope.launch { snackbarHostState.showSnackbar(readonlyMessage) } }, modifier = Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
private fun DateHeader(date: LocalDate) {
    Text(date.format(DateTimeFormatter.ofPattern("M月d日")), color = io.github.takano536.kobako.ui.theme.ThemeTokens.colors.muted, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = PageHorizontalPadding, top = 14.dp, bottom = 4.dp))
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    Row(Modifier.fillMaxWidth().padding(start = PageHorizontalPadding, end = FabContentEndPadding, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        CategoryDot(transaction.category)
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(transaction.category)
            Text(listOfNotNull(transaction.account, transaction.memo).joinToString("・"), color = io.github.takano536.kobako.ui.theme.ThemeTokens.colors.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        Text(signedYen(transaction.amount), color = transactionColor(transaction.amount, transaction.type), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
    }
}
