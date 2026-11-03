package io.github.takano536.kobako.data.repository

import io.github.takano536.kobako.data.api.*
import io.github.takano536.kobako.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ApiRepository(private val api: KobakoApi) : KobakoRepository {
    override suspend fun connect() = runCatching { api.info().let { ServerInfo(it.name, it.apiVersion, Features(it.features.summary, it.features.balances)) } }
    override suspend fun summary(month: String) = runCatching { api.summary(month).toDomain() }
    override suspend fun balances() = runCatching { api.balances().toDomain() }
    override suspend fun transactions(month: String) = runCatching { api.transactions(month).items.map { it.toDomain() } }
}

class DemoRepository(private val today: LocalDate = LocalDate.now()) : KobakoRepository {
    private val month get() = today.toString().take(7)
    private val tx by lazy { listOf(
        Transaction(1, today.withDayOfMonth(1), "給与", "住信SBI", "11月分給与", 350000, "income"),
        Transaction(2, today.withDayOfMonth(2), "家賃", "住信SBI", null, -90000, "expense"),
        Transaction(3, today.withDayOfMonth(3), "食費", "三井住友カード", null, -45000, "expense"),
        Transaction(4, today.withDayOfMonth(4), "外食", "三井住友カード", null, -18000, "expense"),
        Transaction(5, today.withDayOfMonth(5), "光熱費", "住信SBI", null, -12000, "expense"),
        Transaction(6, today.withDayOfMonth(6), "交通", "Suica", null, -8000, "expense"),
        Transaction(7, today.withDayOfMonth(7), "サブスク", "三井住友カード", null, -1200, "expense"),
    ) }
    override suspend fun connect() = Result.success(ServerInfo("kobako demo", 1, Features(true, true)))
    override suspend fun summary(month: String) = Result.success(Summary(month, 350000, 174200, 175800, listOf(ExpenseCategory(1, "食費", 45000), ExpenseCategory(2, "家賃", 90000), ExpenseCategory(3, "外食", 18000), ExpenseCategory(4, "光熱費", 12000), ExpenseCategory(5, "交通", 8000), ExpenseCategory(6, "サブスク", 1200))))
    override suspend fun balances() = Result.success(Balances(BalanceSummary(842000, 85000, 757000), listOf(
        BalanceGroup("銀行", "bank", listOf(BalanceAccount(1, "住信SBI", "bank", 800000, null, null, null, null))),
        BalanceGroup("現金", "cash", listOf(BalanceAccount(2, "財布", "cash", 30000, null, null, null, null))),
        BalanceGroup("クレジットカード", "credit_card", listOf(BalanceAccount(3, "三井住友カード", "credit_card", -85000, 85000, today.withDayOfMonth(27), 12000, NextPayment(today.withDayOfMonth(27), 85000)))),
        BalanceGroup("電子マネー", "electronic_money", listOf(BalanceAccount(4, "Suica", "electronic_money", 12000, null, null, null, null))),
    )))
    override suspend fun transactions(month: String) = Result.success(tx)
}
