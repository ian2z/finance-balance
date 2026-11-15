package br.com.financebalance.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit espelhando a API REST do backend Spring Boot (/api/v1).
 */
interface FinanceApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun registrar(@Body request: RegistroRequest): AuthResponse

    @GET("usuarios/me")
    suspend fun perfil(): UsuarioDto

    @GET("dashboard/resumo")
    suspend fun resumoDashboard(
        @Query("de") de: String,
        @Query("ate") ate: String
    ): ResumoDashboardDto

    @GET("transacoes")
    suspend fun listarTransacoes(
        @Query("texto") texto: String? = null,
        @Query("tipo") tipo: String? = null,
        @Query("situacao") situacao: String? = null,
        @Query("categoriaId") categoriaId: Long? = null,
        @Query("contaId") contaId: Long? = null,
        @Query("de") de: String? = null,
        @Query("ate") ate: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "data,desc"
    ): PaginaResponse<TransacaoDto>

    @POST("transacoes")
    suspend fun criarTransacao(@Body request: TransacaoRequest): TransacaoDto

    @PUT("transacoes/{id}")
    suspend fun atualizarTransacao(
        @Path("id") id: Long,
        @Body request: TransacaoRequest
    ): TransacaoDto

    @DELETE("transacoes/{id}")
    suspend fun excluirTransacao(@Path("id") id: Long)

    @POST("transferencias")
    suspend fun criarTransferencia(@Body request: TransferenciaRequest): TransacaoDto

    @GET("contas")
    suspend fun listarContas(): List<ContaDto>

    @POST("contas")
    suspend fun criarConta(@Body request: ContaRequest): ContaDto

    @GET("categorias")
    suspend fun listarCategorias(): List<CategoriaDto>

    @GET("formas-pagamento")
    suspend fun listarFormasPagamento(): List<RefDto>
}
