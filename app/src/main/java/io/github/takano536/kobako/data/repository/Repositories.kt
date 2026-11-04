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
    override suspend fun fixedCosts(): FixedCostsResult = try {
        FixedCostsResult.Success(api.fixedCosts().toDomain())
    } catch (error: retrofit2.HttpException) {
        if (error.code() == 404) FixedCostsResult.Unsupported else FixedCostsResult.Failure(error)
    } catch (error: Throwable) {
        FixedCostsResult.Failure(error)
    }
}

class DemoRepository(private val today: LocalDate = LocalDate.now()) : KobakoRepository {
    private val month get() = today.toString().take(7)
    private val tx by lazy { listOf(
        Transaction(1, today.withDayOfMonth(27), "住居", "生活費口座", "家賃", -85000, "expense", "api"),
        Transaction(2, today.withDayOfMonth(25), "給与", "給与口座", "給与", 320000, "income"),
        Transaction(3, today.withDayOfMonth(21), "娯楽", "クレジットカード", "書籍", -14500, "expense", "api"),
        Transaction(4, today.withDayOfMonth(18), "臨時収入", "給与口座", "メルカリ売上", 8000, "income"),
        Transaction(5, today.withDayOfMonth(15), "交通", "現金", "定期券", -9500, "expense"),
        Transaction(6, today.withDayOfMonth(26), "振替", "給与口座 → 生活費口座", "生活費へ振替", 180000, "transfer", "api"),
        Transaction(7, today.withDayOfMonth(12), "日用品", "現金", "ドラッグストア", -16000, "expense"),
        Transaction(8, today.withDayOfMonth(10), "振替", "生活費口座 → 現金", "ATMで引き出し", 30000, "transfer"),
        Transaction(9, today.withDayOfMonth(8), "水道・光熱", "生活費口座", "電気・ガス", -12000, "expense"),
        Transaction(10, today.withDayOfMonth(5), "通信", "クレジットカード", "携帯電話", -7300, "expense"),
        Transaction(11, today.withDayOfMonth(1), "食費", "現金", "スーパー", -42500, "expense"),
    ) }
    override suspend fun connect() = Result.success(ServerInfo("kobako demo", 1, Features(true, true)))
    override suspend fun summary(month: String) = Result.success(Summary(month, 328000, 186800, 141200, listOf(
        ExpenseCategory(1, "住居", 85000),
        ExpenseCategory(2, "食費", 42500),
        ExpenseCategory(3, "日用品", 16000),
        ExpenseCategory(4, "娯楽", 14500),
        ExpenseCategory(5, "水道・光熱", 12000),
        ExpenseCategory(6, "交通", 9500),
        ExpenseCategory(7, "通信", 7300),
    )))
    override suspend fun balances() = Result.success(Balances(BalanceSummary(842000, 85000, 757000), listOf(
        BalanceGroup("銀行", "bank", listOf(
            BalanceAccount(1, "生活費口座", "bank", 512000, null, null, null, null),
            BalanceAccount(2, "給与口座", "bank", 245000, null, null, null, null),
        )),
        BalanceGroup("現金", "cash", listOf(BalanceAccount(3, "現金", "cash", 85000, null, null, null, null))),
        BalanceGroup("クレジットカード", "credit_card", listOf(BalanceAccount(4, "クレジットカード", "credit_card", -85000, 85000, today.withDayOfMonth(27), 0, NextPayment(today.withDayOfMonth(27), 85000)))),
    )))
    override suspend fun transactions(month: String) = Result.success(tx)
    override suspend fun fixedCosts() = FixedCostsResult.Success(FixedCosts(
        monthly = 104300,
        annual = 1251600,
        items = listOf(
            FixedCost(1, "住居", "住居", "生活費口座", 85000, 27, 1, "month", today.withMonth(1).withDayOfMonth(1)),
            FixedCost(2, "水道・光熱", "水道・光熱", "生活費口座", 12000, 8, 1, "month", today.withMonth(1).withDayOfMonth(1)),
            FixedCost(3, "通信", "通信", "クレジットカード", 7300, 5, 1, "month", today.withMonth(1).withDayOfMonth(1)),
        ),
    ))
}
