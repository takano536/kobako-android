package io.github.takano536.kobako.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.takano536.kobako.AppState
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.screens.BalancesScreen
import io.github.takano536.kobako.ui.screens.FixedCostsScreen
import io.github.takano536.kobako.ui.screens.OverviewScreen
import io.github.takano536.kobako.ui.screens.SettingsScreen
import io.github.takano536.kobako.ui.screens.TransactionsScreen

internal object Routes {
    const val onboarding = "onboarding"
    const val overview = "overview"
    const val transactions = "transactions"
    const val fixedCosts = "fixed"
    const val balances = "balances"
    const val settings = "settings"
}

private data class NavItem(val route: String, val label: Int, val glyph: String)

private val navItems = listOf(
    NavItem(Routes.overview, R.string.nav_overview, "▦"),
    NavItem(Routes.transactions, R.string.nav_transactions, "≡"),
    NavItem(Routes.fixedCosts, R.string.nav_fixed_costs, "▤"),
    NavItem(Routes.balances, R.string.nav_balances, "▥"),
    NavItem(Routes.settings, R.string.nav_settings, "⚙"),
)

@Composable
fun KobakoApp(
    viewModel: AppViewModel,
    state: AppState,
    requestNotifications: () -> Unit,
) {
    val nav = rememberNavController()
    val start = if (state.connected) Routes.overview else Routes.onboarding
    LaunchedEffect(state.connected) {
        if (state.connected && nav.currentDestination?.route == Routes.onboarding) {
            nav.navigate(Routes.overview) {
                popUpTo(Routes.onboarding) { inclusive = true }
            }
        }
    }
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.onboarding) { OnboardingScreen(viewModel) }
        composable(Routes.overview) { AppShell(nav, viewModel, state, Routes.overview) { OverviewScreen(viewModel, nav) } }
        composable(Routes.transactions) { AppShell(nav, viewModel, state, Routes.transactions) { TransactionsScreen(viewModel) } }
        composable(Routes.fixedCosts) { AppShell(nav, viewModel, state, Routes.fixedCosts) { FixedCostsScreen(viewModel) } }
        composable(Routes.balances) { AppShell(nav, viewModel, state, Routes.balances) { BalancesScreen(viewModel) } }
        composable(Routes.settings) { AppShell(nav, viewModel, state, Routes.settings) { SettingsScreen(viewModel, requestNotifications) } }
    }
}

@Composable
private fun AppShell(
    nav: NavHostController,
    viewModel: AppViewModel,
    state: AppState,
    selected: String,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = item.route == selected,
                        onClick = { nav.navigate(item.route) { launchSingleTop = true } },
                        icon = { Text(item.glyph, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(stringResource(item.label), maxLines = 1) },
                    )
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.settings.demo) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.demo_active), color = MaterialTheme.colorScheme.primary)
                    androidx.compose.material3.TextButton(onClick = {
                        viewModel.exitDemo()
                        nav.navigate(Routes.onboarding) { popUpTo(0) }
                    }) { Text(stringResource(R.string.demo_exit)) }
                }
            }
            content()
        }
    }
}
