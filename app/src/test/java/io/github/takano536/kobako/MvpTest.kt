package io.github.takano536.kobako

import io.github.takano536.kobako.data.api.*
import io.github.takano536.kobako.domain.scheduleLabel
import io.github.takano536.kobako.data.repository.DemoRepository
import okhttp3.ResponseBody.Companion.toResponseBody
import io.github.takano536.kobako.notifications.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class MvpTest {
    @Test fun urlNormalization() { assertEquals("https://example.com", normalizeServerUrl("https://example.com///").getOrThrow()); assertEquals("https://example.com/base", normalizeServerUrl("https://example.com/base/").getOrThrow()); assertTrue(normalizeServerUrl("http://example.com").isFailure); assertTrue(normalizeServerUrl("http://localhost:3000", true).isSuccess) }
    @Test fun apiJsonParsesAmountsAndNullPayment() { val summary = SummaryDto("2026-11", "350000", "210000", "140000", emptyList()).toDomain(); assertEquals(350000, summary.income); val balances = BalancesDto(BalanceSummaryDto("1", "2", "-1"), listOf(BalanceGroupDto("銀行", "bank", listOf(BalanceAccountDto(1, "A", "bank", "1"))))).toDomain(); assertNull(balances.groups.first().accounts.first().nextPayment) }
    @Test fun serverTransactionResponseParses() {
        val json = """{"items":[{"id":1,"type":"expense","amount":1200,"occurredOn":"2026-11-03","categoryId":1,"categoryName":"食費","accountId":1,"accountName":"現金","memo":null,"origin":"manual"},{"id":2,"type":"transfer","amount":5000,"occurredOn":"2026-11-02","fromAccountId":1,"fromAccountName":"銀行","toAccountId":2,"toAccountName":"現金","memo":null,"origin":"manual"}],"count":2}"""
        val dto = Json { ignoreUnknownKeys = true }.decodeFromString<TransactionsDto>(json)
        assertEquals(2, dto.items.size)
        assertEquals(1200L, dto.items[0].amount)
        assertEquals("食費", dto.items[0].toDomain().category)
        assertEquals("振替", dto.items[1].toDomain().category)
        assertEquals("銀行 → 現金", dto.items[1].toDomain().account)
    }
    @Test fun fixedCostsJsonParsesScheduleAndMalformedStartMonth() {
        val json = """{"monthly":"104300","annual":"1251600","items":[{"id":1,"name":"住居","categoryName":"住居","accountName":"生活費口座","amount":"85000","day":27,"intervalCount":1,"intervalUnit":"month","startMonth":"2026-01-01T00:00:00Z"},{"id":2,"name":"保険","accountName":"口座","amount":"12000","day":5,"intervalCount":2,"intervalUnit":"month","startMonth":"not-a-date"},{"id":3,"name":"年会費","accountName":"カード","amount":"24000","day":1,"intervalCount":1,"intervalUnit":"year","startMonth":"2026-02-01"}]}"""
        val costs = Json { ignoreUnknownKeys = true }.decodeFromString<FixedCostsDto>(json).toDomain()
        assertEquals(104300L, costs.monthly)
        assertEquals(3, costs.items.size)
        assertEquals("毎月27日", costs.items[0].scheduleLabel())
        assertEquals("2か月ごと5日", costs.items[1].scheduleLabel())
        assertEquals("毎年2月1日", costs.items[2].scheduleLabel())
        assertNull(costs.items[1].startMonth)
    }

    @Test fun fixedCostsEmptyResponseIsSupported() {
        val costs = FixedCostsDto("0", "0", emptyList()).toDomain()
        assertTrue(costs.items.isEmpty())
    }

    @Test fun fixedCosts404IsRecognizedAsUnsupported() {
        val response = retrofit2.Response.error<FixedCostsDto>(
            404,
            "".toResponseBody(),
        )
        assertEquals(404, retrofit2.HttpException(response).code())
    }
}
