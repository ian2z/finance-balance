package br.com.financebalance.mobile.data.remote

import java.math.BigDecimal

// ---------- Autenticação e Usuário ----------

data class LoginRequest(
    val email: String,
    val senha: String
)

data class RegistroRequest(
    val nome: String,
    val email: String,
    val senha: String
)

data class UsuarioDto(
    val id: Long,
    val nome: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val tipo: String,
    val expiraEm: String,
    val usuario: UsuarioDto
)

// ---------- Referências, Contas e Categorias ----------

data class RefDto(
    val id: Long,
    val nome: String
)

data class ContaDto(
    val id: Long,
    val nome: String,
    val tipo: String,
    val saldoInicial: BigDecimal,
    val saldoAtual: BigDecimal,
    val ativa: Boolean
)

data class ContaRequest(
    val nome: String,
    val tipo: String,
    val saldoInicial: BigDecimal,
    val ativa: Boolean = true
)

data class CategoriaDto(
    val id: Long,
    val nome: String,
    val tipo: String, // "RECEITA" | "DESPESA"
    val categoriaPaiId: Long? = null,
    val doSistema: Boolean = false,
    val semMovimentacoes: Boolean = false
)

// ---------- Transações e Paginação ----------

data class TransacaoDto(
    val id: Long,
    val descricao: String,
    val valor: BigDecimal,
    val tipo: String, // "RECEITA" | "DESPESA" | "TRANSFERENCIA"
    val situacao: String, // "PAGA" | "PENDENTE"
    val data: String,
    val dataVencimento: String? = null,
    val observacao: String? = null,
    val categoria: RefDto? = null,
    val conta: RefDto,
    val contaDestino: RefDto? = null,
    val formaPagamento: RefDto? = null,
    val etiquetas: List<RefDto> = emptyList()
)

data class TransacaoRequest(
    val descricao: String,
    val valor: BigDecimal,
    val tipo: String, // "RECEITA" | "DESPESA"
    val data: String,
    val dataVencimento: String? = null,
    val situacao: String, // "PAGA" | "PENDENTE"
    val categoriaId: Long,
    val contaId: Long,
    val formaPagamentoId: Long? = null,
    val observacao: String? = null,
    val etiquetaIds: List<Long> = emptyList()
)

data class TransferenciaRequest(
    val descricao: String,
    val valor: BigDecimal,
    val data: String,
    val contaOrigemId: Long,
    val contaDestinoId: Long,
    val observacao: String? = null
)

data class PaginaResponse<T>(
    val conteudo: List<T>,
    val pagina: Int,
    val tamanho: Int,
    val totalElementos: Long,
    val totalPaginas: Int
)

// ---------- Dashboard ----------

data class GastoCategoriaDto(
    val categoriaId: Long,
    val categoria: String,
    val valor: BigDecimal,
    val percentual: BigDecimal
)

data class PontoMensalDto(
    val mes: String,
    val receitas: BigDecimal,
    val despesas: BigDecimal
)

data class MaiorGastoDto(
    val id: Long,
    val descricao: String,
    val valor: BigDecimal,
    val data: String,
    val categoria: String?
)

data class OrcamentoConsumidoDto(
    val totalLimite: BigDecimal,
    val totalUtilizado: BigDecimal,
    val percentual: BigDecimal
)

data class ResumoDashboardDto(
    val de: String,
    val ate: String,
    val saldoTotal: BigDecimal,
    val receitas: BigDecimal,
    val despesas: BigDecimal,
    val resultado: BigDecimal,
    val receitasPendentes: BigDecimal,
    val despesasPendentes: BigDecimal,
    val despesasPorCategoria: List<GastoCategoriaDto> = emptyList(),
    val evolucao: List<PontoMensalDto> = emptyList(),
    val maioresGastos: List<MaiorGastoDto> = emptyList(),
    val orcamento: OrcamentoConsumidoDto? = null
)

// ---------- Padrão único de erro da API ----------

data class ErroApiDto(
    val mensagem: String? = null,
    val campos: Map<String, String>? = null
)
