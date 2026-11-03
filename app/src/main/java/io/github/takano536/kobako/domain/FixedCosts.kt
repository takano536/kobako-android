package io.github.takano536.kobako.domain

import java.time.LocalDate

/** A recurring payment returned by /api/v1/fixed-costs. */
data class FixedCost(
    val id: Long,
    val name: String,
    val categoryName: String?,
    val accountName: String,
    val amount: Long,
    val day: Int,
    val intervalCount: Int,
    val intervalUnit: String,
    val startMonth: LocalDate?,
)

data class FixedCosts(
    val monthly: Long,
    val annual: Long,
    val items: List<FixedCost>,
)

sealed interface FixedCostsResult {
    data class Success(val value: FixedCosts) : FixedCostsResult
    data object Unsupported : FixedCostsResult
    data class Failure(val cause: Throwable) : FixedCostsResult
}

/**
 * Keeps the mock's compact Japanese schedule labels while tolerating malformed dates.
 * The API may send a timestamp or an invalid value; callers must never crash while
 * rendering a fixed cost row.
 */
fun FixedCost.scheduleLabel(): String {
    val date = startMonth
    return when {
        intervalUnit == "year" -> {
            date?.let { "毎年${it.monthValue}月${day}日" } ?: "毎年"
        }
        intervalCount >= 2 -> "${intervalCount}か月ごと${day}日"
        else -> "毎月${day}日"
    }
}
