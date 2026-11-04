package io.github.takano536.kobako
import io.github.takano536.kobako.ui.screens.categoryShareLabel

import io.github.takano536.kobako.data.api.*
import io.github.takano536.kobako.data.repository.ApiRepository
import io.github.takano536.kobako.data.repository.DemoRepository
import io.github.takano536.kobako.domain.FixedCostsResult
import io.github.takano536.kobako.domain.scheduleLabel
import io.github.takano536.kobako.ui.theme.categoryDotIndex
import io.github.takano536.kobako.notifications.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDate

class MvpTest {
    @Test fun urlNormalization() { assertEquals("https://example.com", normalizeServerUrl("https://example.com///").getOrThrow()); assertEquals("https://example.com/base", normalizeServerUrl("https://example.com/base/").getOrThrow()); assertTrue(normalizeServerUrl("http://example.com").isFailure); assertTrue(normalizeServerUrl("http://localhost:3000", true).isSuccess) }
    @Test fun apiJsonParsesAmountsAndNullPayment() { val summary = SummaryDto("2026-11", "350000", "210000", "140000", emptyList()).toDomain(); assertEquals(350000, summary.income); val balances = BalancesDto(BalanceSummaryDto("1", "2", "-1"), listOf(BalanceGroupDto("銀行", "bank", listOf(BalanceAccountDto(1, "A", "bank", "1"))))).toDomain(); assertNull(balances.groups.first().accounts.first().nextPayment) }
    @Test fun serverTransactionResponseParses() {
        val json = """{"items":[{"id":1,"type":"expense","amount":1200,"occurredOn":"2026-11-03","categoryId":1,"categoryName":"食費","accountId":1,"accountName":"現金","memo":null,"origin":"api"},{"id":2,"type":"transfer","amount":5000,"occurredOn":"2026-11-02","fromAccountId":1,"fromAccountName":"銀行","toAccountId":2,"toAccountName":"現金","memo":null,"origin":"manual"}],"count":2}"""
        val dto = Json { ignoreUnknownKeys = true }.decodeFromString<TransactionsDto>(json)
        assertEquals(2, dto.items.size)
        assertEquals(1200L, dto.items[0].amount)
        assertEquals("食費", dto.items[0].toDomain().category)
        assertEquals("振替", dto.items[1].toDomain().category)
        assertEquals("銀行 → 現金", dto.items[1].toDomain().account)
        assertEquals("api", dto.items[0].toDomain().origin)
    }

    @Test fun fixedCostsRepositoryParsesContractJson() = runBlocking {
        val result = ApiRepository(StubApi(fixedCosts = FixedCostsDto("104300", "1251600", listOf(FixedCostDto(1, "住居", "住居", "生活費口座", "85000", 27, 1, "month", "2026-01-01"))))).fixedCosts()
        assertTrue(result is FixedCostsResult.Success)
        val costs = (result as FixedCostsResult.Success).value
        assertEquals(104300L, costs.monthly)
        assertEquals(1251600L, costs.annual)
        assertEquals(1, costs.items.size)
        assertEquals("毎月27日", costs.items.single().scheduleLabel())
    }

    @Test fun fixedCostsRepository404IsUnsupported() = runBlocking {
        val error = HttpException(Response.error<FixedCostsDto>(404, "".toResponseBody()))
        assertSame(FixedCostsResult.Unsupported, ApiRepository(StubApi(error = error)).fixedCosts())
    }

    @Test fun fixedCostsRepositoryEmptyItemsIsEmpty() = runBlocking {
        val result = ApiRepository(StubApi(fixedCosts = FixedCostsDto("0", "0", emptyList()))).fixedCosts()
        val costs = (result as FixedCostsResult.Success).value
        assertTrue(costs.items.isEmpty())
    }

    @Test fun fixedCostsStartMonthOnlyUsesDatePrefixAndInvalidValueIsSafe() {
        val json = """{"monthly":"1","annual":"12","items":[{"id":1,"name":"年会費","accountName":"カード","amount":"1","day":1,"intervalCount":1,"intervalUnit":"year","startMonth":"2026-02-01T00:00:00Z"},{"id":2,"name":"保険","accountName":"口座","amount":"1","day":5,"intervalCount":2,"intervalUnit":"month","startMonth":"not-a-date"}]}"""
        val costs = Json { ignoreUnknownKeys = true }.decodeFromString<FixedCostsDto>(json).toDomain()
        assertEquals("毎年2月1日", costs.items[0].scheduleLabel())
        assertEquals("2か月ごと5日", costs.items[1].scheduleLabel())
        assertNull(costs.items[1].startMonth)
    }

    @Test fun demoValues() = runBlocking { val demo = DemoRepository(LocalDate.of(2026, 11, 3)); val summary = demo.summary("2026-11").getOrThrow(); assertEquals(328000, summary.income); assertEquals(186800, summary.expense); assertEquals(141200, summary.difference); val balances = demo.balances().getOrThrow(); assertEquals(842000, balances.summary.assets); assertEquals(757000, balances.summary.net) }
    @Test fun categoryColorsAndShareRounding() {
        assertEquals(0, categoryDotIndex("住居"))
        assertEquals(1, categoryDotIndex("食費"))
        assertEquals(6, categoryDotIndex("通信"))
        assertEquals("33%", categoryShareLabel(1, 3))
        assertEquals("<1%", categoryShareLabel(1, 201))
    }
    @Test fun dueDateAndDeduplication() { val today = LocalDate.of(2026, 11, 26); assertTrue(shouldNotify(today, today)); assertTrue(shouldNotify(today.plusDays(1), today)); assertFalse(shouldNotify(today.plusDays(2), today)); val d = NotificationDeduplicator(); assertTrue(d.markIfNew(2, today)); assertFalse(d.markIfNew(2, today)); assertTrue(d.markIfNew(2, today.plusDays(1))) }

    private class StubApi(private val fixedCosts: FixedCostsDto? = null, private val error: Throwable? = null) : KobakoApi {
        override suspend fun info() = throw UnsupportedOperationException()
        override suspend fun summary(month: String) = throw UnsupportedOperationException()
        override suspend fun balances() = throw UnsupportedOperationException()
        override suspend fun transactions(month: String) = throw UnsupportedOperationException()
        override suspend fun fixedCosts(): FixedCostsDto = error?.let { throw it } ?: requireNotNull(fixedCosts)
    }
}
