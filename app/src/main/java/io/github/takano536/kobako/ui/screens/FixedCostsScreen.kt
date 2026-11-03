package io.github.takano536.kobako.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.domain.FixedCost
import io.github.takano536.kobako.domain.FixedCosts
import io.github.takano536.kobako.domain.scheduleLabel
import io.github.takano536.kobako.ui.theme.ThemeTokens
import retrofit2.HttpException

@Composable
fun FixedCostsScreen(viewModel: AppViewModel) {
    val repository = viewModel.repository()
    var data by remember { mutableStateOf<FixedCosts?>(null) }
    var unsupported by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(repository) {
        data = null
        unsupported = false
        failed = false
        repository?.fixedCosts()?.onSuccess { data = it }?.onFailure {
            unsupported = (it as? HttpException)?.code() == 404
            failed = !unsupported
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 152.dp),
    ) {
        item { Text(stringResource(R.string.nav_fixed_costs), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 10.dp)) }
        when {
            unsupported -> item { Text(stringResource(R.string.fixed_cost_api_unsupported), modifier = Modifier.padding(vertical = 24.dp)) }
            failed -> item { EmptyMessage(stringResource(R.string.read_error)) }
            data == null -> item { EmptyMessage(stringResource(R.string.read_error)) }
            else -> {
                val fixed = requireNotNull(data)
                item {
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Metric(stringResource(R.string.fixed_monthly), fixed.monthly, ThemeTokens.moneyNegative)
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
    Row(
        Modifier.fillMaxWidth().drawBehind {
            drawLine(ThemeTokens.rule, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height))
        }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium)
            Text("${item.accountName}・${item.scheduleLabel()}", color = ThemeTokens.muted, style = MaterialTheme.typography.bodySmall)
        }
        Text(yen(item.amount), color = ThemeTokens.moneyNegative, style = MaterialTheme.typography.bodyMedium)
    }
}
