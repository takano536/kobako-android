package io.github.takano536.kobako.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.security.KeyStore
private val Context.settingsDataStore by preferencesDataStore("settings")
private val URL = stringPreferencesKey("server_url")
private val TOKEN = stringPreferencesKey("encrypted_token")
private val THEME = stringPreferencesKey("theme")
private val NOTIFY = booleanPreferencesKey("notifications")
private val DEMO = booleanPreferencesKey("demo")

class SettingsStore(private val context: Context) {
    val state: Flow<Settings> = context.settingsDataStore.data.map { p -> Settings(p[URL].orEmpty(), p[THEME] ?: "system", p[NOTIFY] ?: false, p[DEMO] ?: false) }
    suspend fun saveConnection(url: String, token: String) { context.settingsDataStore.edit { it[URL] = url; it[TOKEN] = encrypt(token); it[DEMO] = false } }
    suspend fun token(): String? = context.settingsDataStore.data.map { it[TOKEN] }.map { it?.let(::decrypt) }.firstOrNull()
    suspend fun setDemo(enabled: Boolean) { context.settingsDataStore.edit { it[DEMO] = enabled } }
    suspend fun setTheme(theme: String) { context.settingsDataStore.edit { it[THEME] = theme } }
    suspend fun setNotifications(enabled: Boolean) { context.settingsDataStore.edit { it[NOTIFY] = enabled } }
    private fun key(): javax.crypto.SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!ks.containsAlias("kobako_token")) KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply { init(KeyGenParameterSpec.Builder("kobako_token", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build()); generateKey() }
        return (ks.getEntry("kobako_token", null) as KeyStore.SecretKeyEntry).secretKey
    }
    private fun encrypt(value: String): String { val c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key()); return Base64.encodeToString(c.iv + c.doFinal(value.toByteArray()), Base64.NO_WRAP) }
    private fun decrypt(value: String): String? = runCatching { val bytes = Base64.decode(value, Base64.NO_WRAP); Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12))) }.doFinal(bytes.copyOfRange(12, bytes.size)).decodeToString() }.getOrNull()
}
data class Settings(val url: String, val theme: String, val notifications: Boolean, val demo: Boolean)
