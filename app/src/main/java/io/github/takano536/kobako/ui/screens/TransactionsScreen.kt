package io.github.takano536.kobako.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.Transaction
import io.github.takano536.kobako.ui.theme.ThemeTokens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(viewModel: AppViewModel) {
    val repository = viewModel.repository()
    val month by viewModel.monthFlow.collectAsStateWithLifecycle()
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }
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
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 80.dp),
        ) {
            item {
                PageTitle(stringResource(R.string.nav_transactions))
                MonthSelector(month, { viewModel.moveMonth(-1) }, { viewModel.moveMonth(1) })
            }
            if (failed) item { EmptyMessage(stringResource(R.string.read_error)) }
            if (transactions.isEmpty() && !failed) item { EmptyMessage(stringResource(R.string.no_transactions)) }
            transactions.groupBy { it.occurredOn }.toSortedMap(reverseOrder()).forEach { (date, rows) ->
                item(key = date.toString()) { DateHeader(date) }
                items(rows, key = { it.id }) { TransactionRow(it) }
            }
        }
        RegisterFab(fabVisible, Modifier.align(Alignment.BottomEnd))
    }
}

@Composable private fun DateHeader(date: LocalDate) {
    Text(date.format(DateTimeFormatter.ofPattern("M月d日")), color = ThemeTokens.colors.muted, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 14.dp, bottom = 4.dp))
}

@Composable private fun TransactionRow(transaction: Transaction) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(transaction.category)
            Text(listOfNotNull(transaction.account, transaction.memo).joinToString("・"), color = ThemeTokens.colors.muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(signedYen(transaction.amount), color = if (transaction.amount < 0) ThemeTokens.colors.moneyNegative else ThemeTokens.colors.moneyPositive)
    }
}
