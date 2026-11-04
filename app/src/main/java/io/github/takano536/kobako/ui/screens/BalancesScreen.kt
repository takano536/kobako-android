package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.BalanceAccount
import io.github.takano536.kobako.domain.BalanceGroup
import io.github.takano536.kobako.domain.Balances
import io.github.takano536.kobako.ui.Routes
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.ListBottomPadding
@Composable
fun BalancesScreen(viewModel: AppViewModel, nav: NavHostController) {
    val repository = viewModel.repository()
    var balances by remember { mutableStateOf<Balances?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(repository) {
        balances = null
        failed = false
        repository?.balances()?.onSuccess { balances = it }?.onFailure { failed = true }
    }
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = ListBottomPadding)) {
        item { PageHeader(stringResource(R.string.nav_balances)) { nav.navigate(Routes.settings) } }
        if (failed) item { EmptyMessage(stringResource(R.string.read_error)) }
        balances?.let { data ->
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Metric(stringResource(R.string.net_assets), data.summary.net, if (data.summary.net < 0) ThemeTokens.colors.moneyNegative else ThemeTokens.colors.ink, primary = true)
                    Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric(stringResource(R.string.assets), data.summary.assets)
                        Metric(stringResource(R.string.liabilities), data.summary.liabilities, ThemeTokens.colors.moneyNegative)
                    }
                }
            }
            data.groups.forEach { group ->
                item { BalanceHeading(group) }
                items(group.accounts, key = { it.id }) { AccountRow(it, group.systemKey) }
            }
        }
    }
}

@Composable
private fun BalanceHeading(group: BalanceGroup) {
    val columns = when (group.systemKey) {
        "bank" -> listOf(stringResource(R.string.scheduled_payment), stringResource(R.string.balance))
        "credit_card" -> listOf(stringResource(R.string.settlement_scheduled), stringResource(R.string.unbilled))
        else -> listOf(stringResource(R.string.balance))
    }
    Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Text(group.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            columns.forEach { Text(it, color = ThemeTokens.colors.muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.End) }
        }
    }
}

@Composable
private fun AccountRow(account: BalanceAccount, systemKey: String) {
    val columns = when (systemKey) {
        "bank" -> listOf(account.scheduledAmount ?: 0L, account.balance)
        "credit_card" -> listOf(account.scheduledAmount ?: 0L, account.unbilledAmount ?: 0L)
        else -> listOf(account.balance)
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(account.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        columns.forEach { value ->
            Text(yen(value), modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = if (value < 0) ThemeTokens.colors.moneyNegative else ThemeTokens.colors.ink, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
        }
    }
}
