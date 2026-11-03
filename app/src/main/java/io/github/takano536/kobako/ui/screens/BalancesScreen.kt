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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.BalanceAccount
import io.github.takano536.kobako.domain.Balances
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun BalancesScreen(viewModel: AppViewModel) {
    val repository = viewModel.repository()
    var balances by remember { mutableStateOf<Balances?>(null) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(repository) {
        balances = null
        failed = false
        repository?.balances()?.onSuccess { balances = it }?.onFailure { failed = true }
    }
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 152.dp)) {
        item { PageTitle(stringResource(R.string.nav_balances)) }
        balances?.let { data ->
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric(stringResource(R.string.assets), data.summary.assets)
                    Metric(stringResource(R.string.liabilities), data.summary.liabilities, ThemeTokens.colors.moneyNegative)
                    Metric(stringResource(R.string.net_assets), data.summary.net, ThemeTokens.colors.moneyPositive)
                }
            }
            data.groups.forEach { group ->
                item { SectionTitle(group.name) }
                items(group.accounts, key = { it.id }) { AccountRow(it) }
            }
        }
    }
}
@Composable
private fun AccountRow(account: BalanceAccount) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(account.name)
            account.scheduledAmount?.let { Text(stringResource(R.string.scheduled_payment) + " " + yen(it), color = ThemeTokens.colors.muted, style = MaterialTheme.typography.bodySmall) }
        }
        Text(yen(account.balance), color = if (account.balance < 0) ThemeTokens.colors.moneyNegative else MaterialTheme.colorScheme.onSurface)
    }
}
