package io.github.takano536.kobako.domain

import java.time.LocalDate

data class Features(val summary: Boolean = false, val balances: Boolean = false)
data class ServerInfo(val name: String, val apiVersion: Int, val features: Features)
data class ExpenseCategory(val categoryId: Long, val categoryName: String, val total: Long)
data class Summary(val month: String, val income: Long, val expense: Long, val difference: Long, val expenseCategories: List<ExpenseCategory>)
data class NextPayment(val dueOn: LocalDate, val amount: Long)
data class BalanceAccount(val id: Long, val name: String, val kind: String, val balance: Long, val scheduledAmount: Long?, val scheduledDueOn: LocalDate?, val unbilledAmount: Long?, val nextPayment: NextPayment?)
data class BalanceGroup(val name: String, val systemKey: String, val accounts: List<BalanceAccount>)
data class BalanceSummary(val assets: Long, val liabilities: Long, val net: Long)
data class Balances(val summary: BalanceSummary, val groups: List<BalanceGroup>)
data class Transaction(val id: Long, val occurredOn: LocalDate, val category: String, val account: String, val memo: String?, val amount: Long, val type: String)

interface KobakoRepository {
    suspend fun connect(): Result<ServerInfo>
    suspend fun summary(month: String): Result<Summary>
    suspend fun balances(): Result<Balances>
    suspend fun transactions(month: String): Result<List<Transaction>>
    suspend fun fixedCosts(): FixedCostsResult
}
