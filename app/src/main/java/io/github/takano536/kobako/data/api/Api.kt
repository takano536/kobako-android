package io.github.takano536.kobako.data.api

import io.github.takano536.kobako.domain.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType

@Serializable data class FeaturesDto(val summary: Boolean = false, val balances: Boolean = false)
@Serializable data class ServerInfoDto(val name: String = "kobako", val apiVersion: Int = 1, val features: FeaturesDto = FeaturesDto())
@Serializable data class CategoryDto(@SerialName("categoryId") val id: Long, @SerialName("categoryName") val name: String, val total: String)
@Serializable data class SummaryDto(val month: String, val income: String, val expense: String, val difference: String, val expenseCategories: List<CategoryDto> = emptyList())
@Serializable data class PaymentDto(val dueOn: String, val amount: String)
@Serializable data class BalanceAccountDto(val id: Long, val name: String, val kind: String, val balance: String, val scheduledAmount: String? = null, val scheduledDueOn: String? = null, val unbilledAmount: String? = null, val nextPayment: PaymentDto? = null)
@Serializable data class BalanceGroupDto(val name: String, val systemKey: String, val accounts: List<BalanceAccountDto>)
@Serializable data class BalanceSummaryDto(val assets: String, val liabilities: String, val net: String)
@Serializable data class BalancesDto(val summary: BalanceSummaryDto, val groups: List<BalanceGroupDto>)
@Serializable data class TransactionDto(val id: Long, val occurredOn: String, val categoryName: String? = null, val accountName: String? = null, val memo: String? = null, val amount: String, val type: String)

interface KobakoApi {
    @GET("api/v1") suspend fun info(): ServerInfoDto
    @GET("api/v1/summary") suspend fun summary(@Query("month") month: String): SummaryDto
    @GET("api/v1/balances") suspend fun balances(): BalancesDto
    @GET("api/v1/transactions") suspend fun transactions(@Query("month") month: String): List<TransactionDto>
}

class BearerInterceptor(private val token: () -> String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(
        chain.request().newBuilder().header("Authorization", "Bearer ${token()}").build()
    )
}

fun normalizeServerUrl(raw: String, debug: Boolean = false): Result<String> {
    val value = raw.trim().trimEnd('/')
    if (value.isBlank()) return Result.failure(IllegalArgumentException("サーバーURLを入力してください"))
    val uri = runCatching { java.net.URI(value) }.getOrNull()
        ?: return Result.failure(IllegalArgumentException("URLの形式が正しくありません"))
    val allowedLocal = debug && (uri.host == "localhost" || uri.host == "10.0.2.2") && uri.scheme == "http"
    if (uri.scheme != "https" && !allowedLocal) return Result.failure(IllegalArgumentException("https:// のURLを入力してください"))
    if (uri.host.isNullOrBlank()) return Result.failure(IllegalArgumentException("ホスト名がありません"))
    return Result.success(value)
}

fun createApi(baseUrl: String, token: () -> String): KobakoApi {
    val normalized = baseUrl.trimEnd('/') + "/"
    return Retrofit.Builder().baseUrl(normalized).client(okhttp3.OkHttpClient.Builder().addInterceptor(BearerInterceptor(token)).build()).addConverterFactory(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType())).build().create(KobakoApi::class.java)
}

fun SummaryDto.toDomain() = Summary(month, income.toLong(), expense.toLong(), difference.toLong(), expenseCategories.map { ExpenseCategory(it.id, it.name, it.total.toLong()) })
fun BalancesDto.toDomain() = Balances(BalanceSummary(summary.assets.toLong(), summary.liabilities.toLong(), summary.net.toLong()), groups.map { g -> BalanceGroup(g.name, g.systemKey, g.accounts.map { a -> BalanceAccount(a.id, a.name, a.kind, a.balance.toLong(), a.scheduledAmount?.toLong(), a.scheduledDueOn?.let(LocalDate::parse), a.unbilledAmount?.toLong(), a.nextPayment?.let { NextPayment(LocalDate.parse(it.dueOn), it.amount.toLong()) }) }) })
fun TransactionDto.toDomain() = Transaction(id, LocalDate.parse(occurredOn), categoryName ?: "未分類", accountName ?: "不明", memo, amount.toLong(), type)
