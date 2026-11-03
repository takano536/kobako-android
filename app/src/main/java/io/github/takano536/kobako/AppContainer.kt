package io.github.takano536.kobako

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.takano536.kobako.data.api.*
import io.github.takano536.kobako.data.local.*
import io.github.takano536.kobako.data.repository.*
import io.github.takano536.kobako.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

class AppContainer(context: Context) {
    val settings = SettingsStore(context)
    fun apiRepository(url: String, token: String) = ApiRepository(createApi(url) { token })
}

data class AppState(
    val settings: Settings = Settings("", "system", false, false),
    val connected: Boolean = false,
    val repository: KobakoRepository? = null,
    val error: String? = null,
)

class AppViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val _month = MutableStateFlow(YearMonth.now())
    val monthFlow: StateFlow<YearMonth> = _month.asStateFlow()

    init {
        viewModelScope.launch {
            container.settings.state.collect { s ->
                _state.update {
                    it.copy(
                        settings = s,
                        connected = s.demo || it.connected,
                        repository = if (s.demo) DemoRepository() else it.repository,
                    )
                }
            }
        }
    }

    fun enterDemo() {
        viewModelScope.launch {
            container.settings.setDemo(true)
            _state.update { it.copy(connected = true, repository = DemoRepository(), error = null) }
        }
    }

    fun exitDemo() {
        viewModelScope.launch {
            container.settings.setDemo(false)
            _state.update { it.copy(connected = false, repository = null) }
        }
    }

    fun connect(urlInput: String, token: String, debug: Boolean = BuildConfig.DEBUG, onResult: (Boolean) -> Unit = {}) {
        val url = normalizeServerUrl(urlInput, debug).getOrElse {
            _state.update { s -> s.copy(error = it.message) }
            onResult(false)
            return
        }
        viewModelScope.launch {
            val result = container.apiRepository(url, token).connect()
            result.onSuccess { info ->
                if (info.features.summary && info.features.balances) {
                    container.settings.saveConnection(url, token)
                    _state.update { it.copy(connected = true, repository = container.apiRepository(url, token), error = null) }
                    onResult(true)
                } else {
                    _state.update { it.copy(error = "このサーバーは概要・残高APIに対応していません") }
                    onResult(false)
                }
            }.onFailure { failure ->
                _state.update { current ->
                    current.copy(
                        error = when ((failure as? retrofit2.HttpException)?.code()) {
                            401 -> "APIトークンが正しくありません"
                            404 -> "このサーバーは未対応です"
                            503 -> "APIが無効になっています"
                            else -> "接続できませんでした。URLと通信状態を確認してください"
                        },
                    )
                }
                onResult(false)
            }
        }
    }

    fun setTheme(theme: String) { viewModelScope.launch { container.settings.setTheme(theme) } }
    fun setNotifications(enabled: Boolean) { viewModelScope.launch { container.settings.setNotifications(enabled) } }
    fun repository(): KobakoRepository? = _state.value.repository
    fun month(): String = _month.value.toString()
    fun moveMonth(delta: Long) { _month.update { it.plusMonths(delta) } }
    fun resetMonth() { _month.value = YearMonth.now() }
}

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(container) as T
}
