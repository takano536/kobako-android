package io.github.takano536.kobako

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.NavHostController
import io.github.takano536.kobako.domain.*
import io.github.takano536.kobako.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.text.NumberFormat
import java.util.Locale

private fun yen(value: Long): String = NumberFormat.getNumberInstance(Locale.JAPAN).format(value) + "円"
private fun signed(value: Long): String = if (value >= 0) "+${yen(value)}" else "-${yen(-value)}"
private object Routes { const val onboarding = "onboarding"; const val overview = "overview"; const val transactions = "transactions"; const val balances = "balances"; const val settings = "settings" }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); setContent { val container = remember { AppContainer(applicationContext) }; val vm: AppViewModel = viewModel(factory = AppViewModelFactory(container)); val state by vm.state.collectAsStateWithLifecycle(); KobakoTheme(state.settings.theme) { KobakoApp(vm, state, { if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }) } } }
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
}

@Composable private fun KobakoApp(vm: AppViewModel, state: AppState, requestNotifications: () -> Unit) {
    val nav = rememberNavController(); val start = if (state.connected) Routes.overview else Routes.onboarding
    LaunchedEffect(state.connected) { if (state.connected && nav.currentDestination?.route == Routes.onboarding) nav.navigate(Routes.overview) { popUpTo(Routes.onboarding) { inclusive = true } } }
    NavHost(nav, startDestination = start) {
        composable(Routes.onboarding) { OnboardingScreen(vm) }
        composable(Routes.overview) { Shell(nav, vm, state, Routes.overview) { OverviewScreen(vm) } }
        composable(Routes.transactions) { Shell(nav, vm, state, Routes.transactions) { TransactionsScreen(vm) } }
        composable(Routes.balances) { Shell(nav, vm, state, Routes.balances) { BalancesScreen(vm) } }
        composable(Routes.settings) { Shell(nav, vm, state, Routes.settings) { SettingsScreen(vm, requestNotifications) } }
    }
}

@Composable private fun OnboardingScreen(vm: AppViewModel) {
    var url by remember { mutableStateOf("") }; var token by remember { mutableStateOf("") }; var showToken by remember { mutableStateOf(false) }; val state by vm.state.collectAsStateWithLifecycle(); val haptic = LocalHapticFeedback.current
    Column(Modifier.fillMaxSize().padding(40.dp), verticalArrangement = Arrangement.Center) {
        Text("kobako", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Medium); Spacer(Modifier.height(8.dp)); Text("家計を、静かに見渡す", color = ThemeTokens.muted); Spacer(Modifier.height(40.dp))
        OutlinedTextField(url, { url = it }, Modifier.fillMaxWidth(), label = { Text("サーバーURL") }, placeholder = { Text("https://example.com") }, singleLine = true)
        Spacer(Modifier.height(12.dp)); OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("APIトークン") }, singleLine = true, visualTransformation = if (showToken) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(), trailingIcon = { TextButton({ showToken = !showToken }) { Text(if (showToken) "隠す" else "表示") } })
        state.error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = ThemeTokens.moneyNegative) }; Spacer(Modifier.height(24.dp)); Button({ vm.connect(url, token) { ok -> haptic.performHapticFeedback(if (ok) HapticFeedbackType.Confirm else HapticFeedbackType.Reject) } }, Modifier.fillMaxWidth(), enabled = url.isNotBlank() && token.isNotBlank()) { Text("サーバーに接続する") }
        Spacer(Modifier.height(12.dp)); OutlinedButton({ vm.enterDemo() }, Modifier.fillMaxWidth()) { Text("デモを見る") }
    }
}

@Composable private fun Shell(nav: NavHostController, vm: AppViewModel, state: AppState, selected: String, content: @Composable () -> Unit) {
    Scaffold(topBar = { Column { if (state.settings.demo) DemoBanner { vm.exitDemo(); nav.navigate(Routes.onboarding) { popUpTo(0) } }; } }, bottomBar = { NavigationBar { listOf(Routes.overview to "概要", Routes.transactions to "取引", Routes.balances to "残高", Routes.settings to "設定").forEach { (route, label) -> NavigationBarItem(selected == route, { nav.navigate(route) { launchSingleTop = true } }, icon = { Text(if (selected == route) "●" else "○") }, label = { Text(label) }) } } }) { padding -> Box(Modifier.padding(padding)) { content() } }
}
@Composable private fun DemoBanner(onExit: () -> Unit) { Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("デモ表示中", color = ThemeTokens.accent, fontWeight = FontWeight.Bold); TextButton(onExit) { Text("終了") } } }
@Composable private fun Page(title: String, content: @Composable ColumnScope.() -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 24.dp)) { item { Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium); Spacer(Modifier.height(24.dp)) }; item { Column(content = content) } } }
@Composable private fun Metric(label: String, value: Long, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) { Column { Text(label, color = ThemeTokens.muted); Text(yen(value), color = color, style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Medium) } }

@Composable private fun OverviewScreen(vm: AppViewModel) { val repo = vm.repository(); var summary by remember { mutableStateOf<Summary?>(null) }; var error by remember { mutableStateOf<String?>(null) }; LaunchedEffect(repo) { repo?.summary(vm.month())?.onSuccess { summary = it }?.onFailure { error = "読み込めませんでした" } }; Page("概要") { Text(vm.month(), color = ThemeTokens.muted); Spacer(Modifier.height(20.dp)); error?.let { Text(it, color = ThemeTokens.moneyNegative) }; summary?.let { s -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Metric("収入", s.income, ThemeTokens.moneyPositive); Metric("支出", s.expense, ThemeTokens.moneyNegative); Metric("差額", s.difference, if (s.difference >= 0) ThemeTokens.moneyPositive else ThemeTokens.moneyNegative) }; Spacer(Modifier.height(32.dp)); Text("カテゴリ別支出", style = MaterialTheme.typography.titleMedium); s.expenseCategories.forEach { c -> Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(c.categoryName); Text(yen(c.total), style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")) } }; Spacer(Modifier.height(20.dp)); Text("最近の取引", style = MaterialTheme.typography.titleMedium); Text("取引タブで全件を確認できます", color = ThemeTokens.muted) } ?: Text("この月の取引はありません", color = ThemeTokens.muted) } }

@Composable private fun TransactionsScreen(vm: AppViewModel) { val repo = vm.repository(); var tx by remember { mutableStateOf<List<Transaction>?>(null) }; LaunchedEffect(repo) { repo?.transactions(vm.month())?.onSuccess { tx = it } }; Page("取引") { Text(vm.month(), color = ThemeTokens.muted); Spacer(Modifier.height(16.dp)); if (tx.isNullOrEmpty()) Text("この月の取引はありません", color = ThemeTokens.muted) else tx!!.groupBy { it.occurredOn }.forEach { (date, rows) -> Text(date.format(DateTimeFormatter.ofPattern("M月d日")), style = MaterialTheme.typography.titleSmall, color = ThemeTokens.muted, modifier = Modifier.padding(top = 16.dp)); rows.forEach { t -> Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column(Modifier.weight(1f)) { Text(t.category); Text(listOfNotNull(t.account, t.memo).joinToString("・"), color = ThemeTokens.muted, style = MaterialTheme.typography.bodySmall) }; Text(signed(t.amount), color = if (t.amount < 0) ThemeTokens.moneyNegative else ThemeTokens.moneyPositive, style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")) } } } } }

@Composable private fun BalancesScreen(vm: AppViewModel) { val repo = vm.repository(); var balances by remember { mutableStateOf<Balances?>(null) }; LaunchedEffect(repo) { repo?.balances()?.onSuccess { balances = it } }; Page("残高") { balances?.let { b -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Metric("資産", b.summary.assets); Metric("負債", b.summary.liabilities, ThemeTokens.moneyNegative); Metric("純資産", b.summary.net, ThemeTokens.moneyPositive) }; Spacer(Modifier.height(28.dp)); b.groups.forEach { g -> Text(g.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp)); g.accounts.forEach { a -> Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(a.name); Text(yen(a.balance), style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum")) }; Text(a.kind, color = ThemeTokens.muted, style = MaterialTheme.typography.bodySmall); a.nextPayment?.let { Text("次回引落し ${it.dueOn} / ${yen(it.amount)}", color = ThemeTokens.muted, style = MaterialTheme.typography.bodySmall) }; a.unbilledAmount?.let { Text("未請求 ${yen(it)}", color = ThemeTokens.muted, style = MaterialTheme.typography.bodySmall) } } } } } ?: Text("資産が登録されていません", color = ThemeTokens.muted) } }

@Composable private fun SettingsScreen(vm: AppViewModel, requestNotifications: () -> Unit) { val state by vm.state.collectAsStateWithLifecycle(); val scope = rememberCoroutineScope(); Page("設定") { Text("サーバー", style = MaterialTheme.typography.titleMedium); Text(state.settings.url.ifBlank { "未接続" }, color = ThemeTokens.muted); Spacer(Modifier.height(24.dp)); Text("表示", style = MaterialTheme.typography.titleMedium); var expanded by remember { mutableStateOf(false) }; Box { OutlinedButton({ expanded = true }) { Text(mapOf("system" to "システム", "light" to "ライト", "dark" to "ダーク")[state.settings.theme] ?: "システム") }; DropdownMenu(expanded, { expanded = false }) { listOf("system", "light", "dark").forEach { DropdownMenuItem({ Text(it) }, { vm.setTheme(it); expanded = false }) } } }; Spacer(Modifier.height(24.dp)); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { Text("カード支払い通知"); Switch(state.settings.notifications, { enabled -> if (enabled) { requestNotifications(); vm.setNotifications(true) } else vm.setNotifications(false) }) }; if (state.settings.demo) { Spacer(Modifier.height(24.dp)); OutlinedButton({ vm.exitDemo() }) { Text("デモを終了") } }; Spacer(Modifier.height(32.dp)); Text("アプリ情報", style = MaterialTheme.typography.titleMedium); Text("kobako 0.1.0\nAGPL-3.0-only\nhttps://github.com/takano536/kobako", color = ThemeTokens.muted) } }
