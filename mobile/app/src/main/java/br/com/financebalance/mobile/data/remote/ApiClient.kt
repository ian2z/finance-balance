package br.com.financebalance.mobile.data.remote

import br.com.financebalance.mobile.BuildConfig
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var jwtToken: String? = null

    @Volatile
    private var currentBaseUrl: String = BuildConfig.DEFAULT_API_URL

    @Volatile
    private var financeApiInstance: FinanceApi? = null

    private val gson = Gson()

    fun atualizarToken(token: String?) {
        jwtToken = token
    }

    fun atualizarBaseUrl(novaUrl: String) {
        val normalizada = normalizarUrl(novaUrl)
        if (normalizada != currentBaseUrl) {
            currentBaseUrl = normalizada
            financeApiInstance = null
        }
    }

    fun obterBaseUrlAtual(): String = currentBaseUrl

    val api: FinanceApi
        get() = financeApiInstance ?: synchronized(this) {
            financeApiInstance ?: criarRetrofit(currentBaseUrl).create(FinanceApi::class.java).also {
                financeApiInstance = it
            }
        }

    private fun criarRetrofit(baseUrl: String): Retrofit {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
            val token = jwtToken
            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizarUrl(baseUrl))
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun normalizarUrl(url: String): String {
        val limpa = url.trim().ifBlank { BuildConfig.DEFAULT_API_URL }
        return if (limpa.endsWith("/")) limpa else "$limpa/"
    }

    /**
     * Extrai a mensagem amigável do formato único de erro do backend Spring:
     * { "mensagem": "...", "campos": { ... } }
     */
    fun extrairMensagemErro(erro: Throwable): String {
        return when (erro) {
            is HttpException -> {
                val body = try {
                    erro.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }
                if (!body.isNullOrBlank()) {
                    try {
                        val dto = gson.fromJson(body, ErroApiDto::class.java)
                        val detalheCampos = dto?.campos?.entries?.joinToString("; ") { "${it.key}: ${it.value}" }
                        when {
                            !dto?.mensagem.isNullOrBlank() && !detalheCampos.isNullOrBlank() ->
                                "${dto.mensagem} ($detalheCampos)"
                            !dto?.mensagem.isNullOrBlank() -> dto.mensagem!!
                            else -> "Erro HTTP ${erro.code()}"
                        }
                    } catch (_: Exception) {
                        "Erro HTTP ${erro.code()} na comunicação com o servidor."
                    }
                } else {
                    when (erro.code()) {
                        401 -> "Sessão expirada ou credenciais inválidas (401)."
                        403 -> "Acesso negado (403)."
                        404 -> "Recurso não encontrado (404)."
                        else -> "Erro HTTP ${erro.code()}."
                    }
                }
            }
            is ConnectException ->
                "Não foi possível conectar ao backend Spring em $currentBaseUrl. Verifique se o servidor está rodando."
            is SocketTimeoutException ->
                "Tempo de resposta esgotado ao conectar em $currentBaseUrl."
            else -> erro.localizedMessage ?: "Ocorreu um erro inesperado."
        }
    }
}
