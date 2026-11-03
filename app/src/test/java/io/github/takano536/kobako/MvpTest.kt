package io.github.takano536.kobako

import io.github.takano536.kobako.data.api.*
import io.github.takano536.kobako.data.repository.DemoRepository
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
    @Test fun demoValues() = runBlocking { val demo = DemoRepository(LocalDate.of(2026, 11, 3)); val summary = demo.summary("2026-11").getOrThrow(); assertEquals(350000, summary.income); assertEquals(174200, summary.expense); assertEquals(175800, summary.difference); val balances = demo.balances().getOrThrow(); assertEquals(842000, balances.summary.assets); assertEquals(757000, balances.summary.net) }
    @Test fun dueDateAndDeduplication() { val today = LocalDate.of(2026, 11, 26); assertTrue(shouldNotify(today, today)); assertTrue(shouldNotify(today.plusDays(1), today)); assertFalse(shouldNotify(today.plusDays(2), today)); val d = NotificationDeduplicator(); assertTrue(d.markIfNew(2, today)); assertFalse(d.markIfNew(2, today)); assertTrue(d.markIfNew(2, today.plusDays(1))) }
}
