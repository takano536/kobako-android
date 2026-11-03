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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import androidx.compose.runtime.snapshotFlow
import io.github.takano536.kobako.domain.Summary
import io.github.takano536.kobako.domain.Transaction
import io.github.takano536.kobako.ui.Routes
import io.github.takano536.kobako.ui.theme.ThemeTokens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.format.DateTimeFormatter

@Composable
fun OverviewScreen(viewModel: AppViewModel, nav: NavHostController) {
    val repository = viewModel.repository()
    val month by viewModel.monthFlow.collectAsStateWithLifecycle()
    var summary by remember { mutableStateOf<Summary?>(null) }
    var recent by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var error by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var fabVisible by remember { mutableStateOf(true) }

    LaunchedEffect(repository, month) {
        summary = null
        recent = emptyList()
        error = false
        repository?.summary(month.toString())?.onSuccess { summary = it }?.onFailure { error = true }
        repository?.transactions(month.toString())?.onSuccess { recent = it.take(3) }
    }
    LaunchedEffect(listState) {
        var previous = 0
        snapshotFlow { listState.firstVisibleItemIndex * 10000 + listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collectLatest { position ->
                fabVisible = position <= 8 || position < previous
                previous = position
            }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 152.dp),
        ) {
            item {
                Text(stringResource(R.string.nav_overview), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 10.dp))
                MonthSelector(month, { viewModel.moveMonth(-1) }, { viewModel.moveMonth(1) })
            }
            if (error) item { EmptyMessage(stringResource(R.string.read_error)) }
            summary?.let { data ->
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric(stringResource(R.string.income), data.income, ThemeTokens.moneyPositive)
                        Metric(stringResource(R.string.expense), data.expense, ThemeTokens.moneyNegative)
                        Metric(stringResource(R.string.difference), data.difference, if (data.difference >= 0) ThemeTokens.moneyPositive else ThemeTokens.moneyNegative)
                    }
                }
                item { SectionTitle(stringResource(R.string.category_breakdown)) }
                val max = data.expenseCategories.maxOfOrNull { it.total }?.coerceAtLeast(1L) ?: 1L
                items(data.expenseCategories.take(7), key = { it.categoryId }) { category -> CategoryRow(category, data.expense, max) }
                item {
                    SectionTitle(
                        stringResource(R.string.recent_transactions),
                        trailing = {
                            androidx.compose.material3.TextButton(onClick = { nav.navigate(Routes.transactions) }) {
                                Text(stringResource(R.string.all_transactions))
                            }
                        },
                    )
                }
                if (recent.isEmpty()) item { EmptyMessage(stringResource(R.string.no_transactions)) }
                else items(recent, key = { it.id }) { transaction -> RecentRow(transaction) }
            } ?: item { EmptyMessage(stringResource(R.string.no_transactions)) }
        }
        AnimatedVisibility(
            visible = fabVisible,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 88.dp),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
        ) {
            FloatingActionButton(
                onClick = { /* Read-only MVP: registration is intentionally not implemented. */ },
                containerColor = ThemeTokens.accent,
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
            ) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        }
    }
}

@Composable
private fun CategoryRow(category: ExpenseCategory, total: Long, max: Long) {
    Column(Modifier.fillMaxWidth().height(49.dp), verticalArrangement = Arrangement.Center) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(ThemeTokens.categoryDot))
            Text(category.categoryName, Modifier.weight(1f).padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
            Text(yen(category.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                " ${if (total == 0L) 0 else category.total * 100 / total}%",
                color = ThemeTokens.muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp).height(4.dp).clip(CircleShape).background(ThemeTokens.bar)) {
            Box(Modifier.fillMaxWidth(category.total.toFloat() / max).height(4.dp).clip(CircleShape).background(ThemeTokens.categoryBar))
        }
    }
}

@Composable
private fun RecentRow(transaction: Transaction) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(transaction.category, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${transaction.occurredOn.format(DateTimeFormatter.ofPattern("M月d日"))}・${transaction.account}",
                color = ThemeTokens.muted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(signedYen(transaction.amount), color = if (transaction.amount < 0) ThemeTokens.moneyNegative else ThemeTokens.moneyPositive)
    }
}
