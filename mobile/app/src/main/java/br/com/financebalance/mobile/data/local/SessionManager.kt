package br.com.financebalance.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.financebalance.mobile.BuildConfig
import br.com.financebalance.mobile.data.remote.UsuarioDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "finance_balance_session")

data class UserSession(
    val token: String? = null,
    val usuarioId: Long? = null,
    val nome: String? = null,
    val email: String? = null,
    val apiUrl: String = BuildConfig.DEFAULT_API_URL
) {
    val autenticado: Boolean
        get() = !token.isNullOrBlank() && usuarioId != null
}

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_NOME = stringPreferencesKey("user_nome")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_API_URL = stringPreferencesKey("api_base_url")
    }

    val sessionFlow: Flow<UserSession> = context.dataStore.data.map { prefs ->
        UserSession(
            token = prefs[KEY_TOKEN],
            usuarioId = prefs[KEY_USER_ID],
            nome = prefs[KEY_USER_NOME],
            email = prefs[KEY_USER_EMAIL],
            apiUrl = prefs[KEY_API_URL] ?: BuildConfig.DEFAULT_API_URL
        )
    }

    suspend fun salvarSessao(token: String, usuario: UsuarioDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = usuario.id
            prefs[KEY_USER_NOME] = usuario.nome
            prefs[KEY_USER_EMAIL] = usuario.email
        }
    }

    suspend fun salvarApiUrl(novaUrl: String) {
        val urlNormalizada = if (novaUrl.trim().endsWith("/")) novaUrl.trim() else "${novaUrl.trim()}/"
        context.dataStore.edit { prefs ->
            prefs[KEY_API_URL] = urlNormalizada
        }
    }

    suspend fun limparSessao() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_NOME)
            prefs.remove(KEY_USER_EMAIL)
        }
    }
}
