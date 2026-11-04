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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.FixedCost
import io.github.takano536.kobako.domain.FixedCosts
import io.github.takano536.kobako.domain.FixedCostsResult
import io.github.takano536.kobako.domain.scheduleLabel
import io.github.takano536.kobako.ui.Routes
import io.github.takano536.kobako.ui.theme.ThemeTokens

@Composable
fun FixedCostsScreen(viewModel: AppViewModel, nav: NavHostController) {
    val repository = viewModel.repository()
    var data by remember { mutableStateOf<FixedCosts?>(null) }
    var unsupported by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
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
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(bottom = 88.dp)) {
        item { PageHeader(stringResource(R.string.nav_fixed_costs)) { nav.navigate(Routes.settings) } }
        when {
            unsupported -> item { EmptyMessage(stringResource(R.string.fixed_cost_api_unsupported)) }
            failed -> item { EmptyMessage(stringResource(R.string.read_error)) }
            data != null -> {
                val fixed = requireNotNull(data)
                item {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Metric(stringResource(R.string.fixed_monthly), fixed.monthly, ThemeTokens.colors.moneyNegative, primary = true)
                        Metric(stringResource(R.string.annualized), fixed.annual)
                    }
                }
                item { SectionTitle(stringResource(R.string.fixed_breakdown)) }
                if (fixed.items.isEmpty()) item { EmptyMessage(stringResource(R.string.no_fixed_costs)) }
                else items(fixed.items, key = { it.id }) { FixedCostRow(it) }
            }
        }
    }
}

@Composable
private fun FixedCostRow(item: FixedCost) {
    val colors = ThemeTokens.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium)
            Text("${item.accountName}・${item.scheduleLabel()}", color = colors.muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(yen(item.amount), color = colors.moneyNegative, style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"))
    }
}
