package io.github.takano536.kobako.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.takano536.kobako.AppState
import io.github.takano536.kobako.AppViewModel
import io.github.takano536.kobako.R
import io.github.takano536.kobako.ui.screens.BalancesScreen
import io.github.takano536.kobako.ui.screens.DemoHeaderState
import io.github.takano536.kobako.ui.screens.FixedCostsScreen
import io.github.takano536.kobako.ui.screens.LocalDemoHeaderState
import io.github.takano536.kobako.ui.screens.OverviewScreen
import io.github.takano536.kobako.ui.screens.SettingsScreen
import io.github.takano536.kobako.ui.screens.TransactionsScreen
import io.github.takano536.kobako.ui.theme.ThemeTokens
import io.github.takano536.kobako.ui.theme.KobakoText

internal object Routes {
    const val onboarding = "onboarding"
    const val overview = "overview"
    const val transactions = "transactions"
    const val fixedCosts = "fixed"
    const val balances = "balances"
    const val settings = "settings"
}

private data class NavItem(val route: String, val label: Int, val icon: Int)

private val navItems = listOf(
    NavItem(Routes.overview, R.string.nav_overview, R.drawable.ic_nav_overview),
    NavItem(Routes.transactions, R.string.nav_transactions, R.drawable.ic_nav_transactions),
    NavItem(Routes.fixedCosts, R.string.nav_fixed_costs, R.drawable.ic_nav_fixed),
    NavItem(Routes.balances, R.string.nav_balances, R.drawable.ic_nav_balances),
    NavItem(Routes.settings, R.string.nav_settings, R.drawable.ic_nav_settings),
)

@Composable
fun KobakoApp(viewModel: AppViewModel, state: AppState, requestNotifications: () -> Unit) {
    val nav = rememberNavController()
    val start = if (state.connected) Routes.overview else Routes.onboarding
    LaunchedEffect(state.connected) {
        if (state.connected && nav.currentDestination?.route == Routes.onboarding) {
            nav.navigate(Routes.overview) { popUpTo(Routes.onboarding) { inclusive = true } }
        }
    }
    NavHost(navController = nav, startDestination = start) {
        composable(Routes.onboarding) { OnboardingScreen(viewModel) }
        composable(Routes.overview) { AppShell(nav, viewModel, state, Routes.overview) { host -> OverviewScreen(viewModel, nav, host) } }
        composable(Routes.transactions) { AppShell(nav, viewModel, state, Routes.transactions) { host -> TransactionsScreen(viewModel, nav, host) } }
        composable(Routes.fixedCosts) { AppShell(nav, viewModel, state, Routes.fixedCosts) { FixedCostsScreen(viewModel, nav) } }
        composable(Routes.balances) { AppShell(nav, viewModel, state, Routes.balances) { BalancesScreen(viewModel, nav) } }
        composable(Routes.settings) { AppShell(nav, viewModel, state, Routes.settings) { SettingsScreen(viewModel, nav, requestNotifications) } }
    }
}

@Composable
private fun AppShell(nav: NavHostController, viewModel: AppViewModel, state: AppState, selected: String, content: @Composable (SnackbarHostState) -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val colors = ThemeTokens.colors
            Column(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).height(56.dp).background(MaterialTheme.colorScheme.background).drawBehind {
                    val stroke = 1.dp.toPx(); drawLine(colors.rule, Offset(0f, stroke / 2f), Offset(size.width, stroke / 2f), strokeWidth = stroke)
                },
            ) {
                Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                    navItems.forEach { item ->
                        val label = stringResource(item.label)
                        val isSelected = item.route == selected
                        val tint = if (isSelected) colors.accent else colors.muted
                        Column(
                            Modifier.weight(1f).fillMaxHeight().selectable(selected = isSelected, role = Role.Tab, onClick = {
                                nav.navigate(item.route) { popUpTo(Routes.overview); launchSingleTop = true }
                            }),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(painterResource(item.icon), contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
                            Text(label, color = tint, style = KobakoText.small, fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal, maxLines = 1)
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            CompositionLocalProvider(
                LocalDemoHeaderState provides DemoHeaderState(
                    active = state.settings.demo,
                    onExit = {
                        viewModel.exitDemo()
                        nav.navigate(Routes.onboarding) { popUpTo(0) }
                    },
                ),
            ) {
                content(snackbarHostState)
            }
        }
    }
}
