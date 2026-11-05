package io.github.takano536.kobako.ui.screens
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
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
        item { PageHeader(stringResource(R.string.nav_balances)) }
        if (failed) item { EmptyMessage(stringResource(R.string.read_error)) }
        balances?.let { data ->
            item {
                SummaryPanel(topPadding = 7.dp, bottomPadding = 17.dp) {
                    Metric(stringResource(R.string.net_assets), data.summary.net, ThemeTokens.colors.ink, primary = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Metric(stringResource(R.string.assets), data.summary.assets)
                        Metric(stringResource(R.string.liabilities), -kotlin.math.abs(data.summary.liabilities), ThemeTokens.colors.moneyNegative, signed = true)
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
    val topPadding = when (group.systemKey) {
        "cash" -> 33.dp
        "credit_card" -> 31.dp
        else -> 29.dp
    }
    Column(Modifier.fillMaxWidth().padding(top = topPadding)) {
        Text(group.name, color = ThemeTokens.colors.muted, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium, modifier = Modifier.padding(horizontal = 16.dp))
        Box(Modifier.fillMaxWidth().heightIn(min = 24.dp).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(0.6f)) { columns.forEach { Text(it, color = ThemeTokens.colors.muted, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End) } }
            }
        }
    }
}

@Composable
private fun AccountRow(account: BalanceAccount, systemKey: String) {
    val columns = when (systemKey) {
        "bank" -> listOf(account.scheduledAmount ?: 0L, account.balance)
        "credit_card" -> listOf(-kotlin.math.abs(account.scheduledAmount ?: 0L), account.unbilledAmount ?: 0L)
        else -> listOf(account.balance)
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(account.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(0.6f)) {
                columns.forEach { value ->
                    Text(if (value < 0) signedYen(value) else yen(value), modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = ThemeTokens.colors.ink, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
                }
            }
        }
        HorizontalDivider(color = ThemeTokens.colors.rule, thickness = 1.dp)
    }
}
