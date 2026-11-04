package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.Summary
import io.github.takano536.kobako.domain.Transaction
import io.github.takano536.kobako.ui.Routes
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.ListBottomPadding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.time.format.DateTimeFormatter

@Composable
fun OverviewScreen(viewModel: AppViewModel, nav: NavHostController, snackbarHostState: SnackbarHostState) {
    val repository = viewModel.repository()
    val month by viewModel.monthFlow.collectAsStateWithLifecycle()
    var summary by remember { mutableStateOf<Summary?>(null) }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val readonlyMessage = stringResource(R.string.readonly_notice)
    LaunchedEffect(repository, month) {
        summary = null
        transactions = emptyList()
        failed = false
        repository?.summary(month.toString())?.onSuccess { summary = it }?.onFailure { failed = true }
        repository?.transactions(month.toString())?.onSuccess { transactions = it }
    }
    LaunchedEffect(listState) {
        var previous = 0
        snapshotFlow { listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged().collectLatest { position ->
                fabVisible = position <= 8 || position < previous
                previous = position
            }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
            item { PageHeader(stringResource(R.string.nav_overview)) { nav.navigate(Routes.settings) } }
            item { MonthSelector(month, { viewModel.moveMonth(-1) }, { viewModel.moveMonth(1) }) }
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                    when {
                        failed -> EmptyMessage(stringResource(R.string.read_error))
                        summary == null -> EmptyMessage(stringResource(R.string.loading))
                        else -> {
                            val data = requireNotNull(summary)
                            Metric(stringResource(R.string.monthly_expense), data.expense, ThemeTokens.colors.moneyNegative, primary = true)
                            Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                                androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
                                    Metric(stringResource(R.string.income), data.income, ThemeTokens.colors.moneyPositive)
                                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                                    Metric(stringResource(R.string.difference), data.difference, if (data.difference < 0) ThemeTokens.colors.moneyNegative else ThemeTokens.colors.moneyPositive)
                                }
                            }
                        }
                    }
                }
            }
            summary?.let { data ->
                item {
                    SectionTitle(stringResource(R.string.category_breakdown), trailing = {
                        androidx.compose.material3.TextButton(onClick = { nav.navigate(Routes.transactions) }) { Text(stringResource(R.string.see_all)) }
                    })
                }
                items(data.expenseCategories.take(7), key = { "category-${it.categoryId}" }) { category -> CategoryRow(category.categoryName, category.total, data.expense) }
                item {
                    SectionTitle(stringResource(R.string.recent_transactions), trailing = {
                        androidx.compose.material3.TextButton(onClick = { nav.navigate(Routes.transactions) }) { Text(stringResource(R.string.all_transactions)) }
                    })
                }
                if (transactions.isEmpty()) item { EmptyMessage(stringResource(R.string.no_transactions)) }
                else items(transactions.take(5), key = { "recent-transaction-${it.id}" }) { tx ->
                    RecentTransactionRow(tx.occurredOn.format(DateTimeFormatter.ofPattern("M月d日")), tx.category, tx.account, tx.memo, tx.amount, tx.type)
                }
            }
        }
        ReadOnlyFab(fabVisible, onClick = { scope.launch { snackbarHostState.showSnackbar(readonlyMessage) } }, modifier = Modifier.align(Alignment.BottomEnd))
    }
}
