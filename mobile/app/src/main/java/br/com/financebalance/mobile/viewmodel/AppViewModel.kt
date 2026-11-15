package br.com.financebalance.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.financebalance.mobile.BuildConfig
import br.com.financebalance.mobile.data.firestore.MetaFinanceira
import br.com.financebalance.mobile.data.firestore.MetasFirestoreRepository
import br.com.financebalance.mobile.data.local.SessionManager
import br.com.financebalance.mobile.data.local.UserSession
import br.com.financebalance.mobile.data.remote.*
import br.com.financebalance.mobile.util.deslocarMes
import br.com.financebalance.mobile.util.limitesDoMes
import br.com.financebalance.mobile.util.mesAtualIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class FiltroTransacoesState(
    val texto: String = "",
    val tipo: String? = null,       // null = Todos, "RECEITA", "DESPESA", "TRANSFERENCIA"
    val situacao: String? = null,   // null = Todas, "PAGA", "PENDENTE"
    val categoriaId: Long? = null
)

data class FiltroMetasState(
    val texto: String = "",
    val status: String = "TODAS",     // "TODAS" | "EM_ANDAMENTO" | "CONCLUIDAS"
    val prioridade: String? = null    // null | "ALTA" | "MEDIA" | "BAIXA"
)

data class AppUiState(
    val sessaoCarregada: Boolean = false,
    val sessao: UserSession = UserSession(),
    val carregandoAuth: Boolean = false,
    val erroAuth: String? = null,

    // Dados vindos da API REST (Spring Boot via Retrofit)
    val mesDashboard: String = mesAtualIso(),
    val carregandoDashboard: Boolean = false,
    val dashboard: ResumoDashboardDto? = null,
    val contas: List<ContaDto> = emptyList(),
    val categorias: List<CategoriaDto> = emptyList(),
    val formasPagamento: List<RefDto> = emptyList(),

    val carregandoTransacoes: Boolean = false,
    val transacoes: List<TransacaoDto> = emptyList(),
    val totalTransacoes: Long = 0,
    val filtroTransacoes: FiltroTransacoesState = FiltroTransacoesState(),

    // Dados vindos do Cloud Firestore (Metas Financeiras)
    val carregandoMetas: Boolean = false,
    val metas: List<MetaFinanceira> = emptyList(),
    val filtroMetas: FiltroMetasState = FiltroMetasState(),

    // Feedback global (Snackbar / mensagens)
    val mensagemFeedback: String? = null,
    val erroOperacao: String? = null
) {
    val metasFiltradas: List<MetaFinanceira>
        get() = metas.filter { meta ->
            val bateTexto = filtroMetas.texto.isBlank() ||
                meta.titulo.contains(filtroMetas.texto, ignoreCase = true) ||
                meta.categoria.contains(filtroMetas.texto, ignoreCase = true) ||
                meta.observacao.contains(filtroMetas.texto, ignoreCase = true)

            val bateStatus = when (filtroMetas.status) {
                "EM_ANDAMENTO" -> !meta.concluida
                "CONCLUIDAS" -> meta.concluida
                else -> true
            }

            val batePrioridade = filtroMetas.prioridade == null || meta.prioridade == filtroMetas.prioridade
            bateTexto && bateStatus && batePrioridade
        }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val firestoreRepository = MetasFirestoreRepository()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.sessionFlow.collect { sessao ->
                ApiClient.atualizarBaseUrl(sessao.apiUrl)
                ApiClient.atualizarToken(sessao.token)
                val estavaAutenticado = _uiState.value.sessao.autenticado
                _uiState.update {
                    it.copy(sessaoCarregada = true, sessao = sessao)
                }
                if (sessao.autenticado && !estavaAutenticado) {
                    carregarTudo()
                }
            }
        }
    }

    fun limparMensagens() {
        _uiState.update { it.copy(mensagemFeedback = null, erroOperacao = null, erroAuth = null) }
    }

    fun atualizarUrlApi(novaUrl: String) {
        viewModelScope.launch {
            val urlLimpa = novaUrl.trim().ifBlank { BuildConfig.DEFAULT_API_URL }
            sessionManager.salvarApiUrl(urlLimpa)
            ApiClient.atualizarBaseUrl(urlLimpa)
            _uiState.update { it.copy(mensagemFeedback = "Endereço do servidor atualizado.") }
        }
    }

    // ==================== AUTENTICAÇÃO (Retrofit -> Spring Boot) ====================

    fun login(email: String, senha: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoAuth = true, erroAuth = null) }
            try {
                val resp = ApiClient.api.login(LoginRequest(email.trim(), senha))
                ApiClient.atualizarToken(resp.token)
                sessionManager.salvarSessao(resp.token, resp.usuario)
                _uiState.update { it.copy(carregandoAuth = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregandoAuth = false,
                        erroAuth = ApiClient.extrairMensagemErro(e)
                    )
                }
            }
        }
    }

    fun registrar(nome: String, email: String, senha: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoAuth = true, erroAuth = null) }
            try {
                val resp = ApiClient.api.registrar(RegistroRequest(nome.trim(), email.trim(), senha))
                ApiClient.atualizarToken(resp.token)
                sessionManager.salvarSessao(resp.token, resp.usuario)
                _uiState.update { it.copy(carregandoAuth = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregandoAuth = false,
                        erroAuth = ApiClient.extrairMensagemErro(e)
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.limparSessao()
            ApiClient.atualizarToken(null)
            _uiState.update {
                AppUiState(
                    sessaoCarregada = true,
                    sessao = UserSession(apiUrl = it.sessao.apiUrl)
                )
            }
        }
    }

    // ==================== CARREGAMENTO DE DADOS ====================

    fun carregarTudo() {
        carregarDashboardECadastros()
        carregarTransacoes()
        carregarMetasFirestore()
    }

    fun alterarMesDashboard(delta: Long) {
        val novoMes = deslocarMes(_uiState.value.mesDashboard, delta)
        _uiState.update { it.copy(mesDashboard = novoMes) }
        carregarDashboardECadastros()
    }

    fun carregarDashboardECadastros() {
        val mes = _uiState.value.mesDashboard
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoDashboard = true) }
            try {
                val (de, ate) = limitesDoMes(mes)
                val resumo = ApiClient.api.resumoDashboard(de, ate)
                val contas = ApiClient.api.listarContas()
                val categorias = ApiClient.api.listarCategorias()
                val formas = ApiClient.api.listarFormasPagamento()

                _uiState.update {
                    it.copy(
                        carregandoDashboard = false,
                        dashboard = resumo,
                        contas = contas,
                        categorias = categorias,
                        formasPagamento = formas
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregandoDashboard = false,
                        erroOperacao = ApiClient.extrairMensagemErro(e)
                    )
                }
            }
        }
    }

    // ==================== TRANSAÇÕES COM BUSCA, FILTRO E CRUD (Retrofit) ====================

    fun atualizarFiltroTransacoes(novoFiltro: FiltroTransacoesState) {
        _uiState.update { it.copy(filtroTransacoes = novoFiltro) }
        carregarTransacoes()
    }

    fun carregarTransacoes() {
        val filtro = _uiState.value.filtroTransacoes
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoTransacoes = true) }
            try {
                val pagina = ApiClient.api.listarTransacoes(
                    texto = filtro.texto.trim().ifBlank { null },
                    tipo = filtro.tipo,
                    situacao = filtro.situacao,
                    categoriaId = filtro.categoriaId,
                    size = 50
                )
                _uiState.update {
                    it.copy(
                        carregandoTransacoes = false,
                        transacoes = pagina.conteudo,
                        totalTransacoes = pagina.totalElementos
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregandoTransacoes = false,
                        erroOperacao = ApiClient.extrairMensagemErro(e)
                    )
                }
            }
        }
    }

    fun salvarTransacao(idEdicao: Long?, request: TransacaoRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            try {
                if (idEdicao == null) {
                    ApiClient.api.criarTransacao(request)
                } else {
                    ApiClient.api.atualizarTransacao(idEdicao, request)
                }
                _uiState.update {
                    it.copy(
                        mensagemFeedback = if (idEdicao == null) "Transação criada." else "Transação atualizada."
                    )
                }
                onSucesso()
                carregarTransacoes()
                carregarDashboardECadastros()
            } catch (e: Exception) {
                _uiState.update { it.copy(erroOperacao = ApiClient.extrairMensagemErro(e)) }
            }
        }
    }

    fun salvarTransferencia(request: TransferenciaRequest, onSucesso: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.criarTransferencia(request)
                _uiState.update { it.copy(mensagemFeedback = "Transferência registrada.") }
                onSucesso()
                carregarTransacoes()
                carregarDashboardECadastros()
            } catch (e: Exception) {
                _uiState.update { it.copy(erroOperacao = ApiClient.extrairMensagemErro(e)) }
            }
        }
    }

    fun excluirTransacao(id: Long) {
        viewModelScope.launch {
            try {
                ApiClient.api.excluirTransacao(id)
                _uiState.update { it.copy(mensagemFeedback = "Transação excluída.") }
                carregarTransacoes()
                carregarDashboardECadastros()
            } catch (e: Exception) {
                _uiState.update { it.copy(erroOperacao = ApiClient.extrairMensagemErro(e)) }
            }
        }
    }

    fun criarContaRapida(nome: String, tipo: String, saldoInicial: BigDecimal, onSucesso: () -> Unit) {
        viewModelScope.launch {
            try {
                ApiClient.api.criarConta(ContaRequest(nome.trim(), tipo, saldoInicial, true))
                _uiState.update { it.copy(mensagemFeedback = "Conta criada.") }
                onSucesso()
                carregarDashboardECadastros()
            } catch (e: Exception) {
                _uiState.update { it.copy(erroOperacao = ApiClient.extrairMensagemErro(e)) }
            }
        }
    }

    // ==================== METAS FINANCEIRAS — CRUD NO CLOUD FIRESTORE ====================

    fun atualizarFiltroMetas(novoFiltro: FiltroMetasState) {
        _uiState.update { it.copy(filtroMetas = novoFiltro) }
    }

    fun carregarMetasFirestore() {
        val usuarioId = _uiState.value.sessao.usuarioId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(carregandoMetas = true) }
            try {
                val lista = firestoreRepository.listarPorUsuario(usuarioId)
                _uiState.update {
                    it.copy(
                        carregandoMetas = false,
                        metas = lista
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        carregandoMetas = false,
                        erroOperacao = "Falha ao consultar metas: ${e.localizedMessage ?: "verifique a conexão"}"
                    )
                }
            }
        }
    }

    fun salvarMetaFirestore(meta: MetaFinanceira, onSucesso: () -> Unit) {
        val sessao = _uiState.value.sessao
        val usuarioId = sessao.usuarioId ?: return
        viewModelScope.launch {
            try {
                val metaComUsuario = meta.copy(
                    usuarioId = usuarioId,
                    usuarioEmail = sessao.email ?: ""
                )
                if (metaComUsuario.id.isBlank()) {
                    firestoreRepository.criar(metaComUsuario)
                    _uiState.update { it.copy(mensagemFeedback = "Meta criada.") }
                } else {
                    firestoreRepository.atualizar(metaComUsuario)
                    _uiState.update { it.copy(mensagemFeedback = "Meta atualizada.") }
                }
                onSucesso()
                carregarMetasFirestore()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erroOperacao = "Falha ao salvar meta: ${e.localizedMessage}")
                }
            }
        }
    }

    fun adicionarAporteMeta(meta: MetaFinanceira, valorAporte: Double) {
        viewModelScope.launch {
            try {
                firestoreRepository.registrarAporte(meta, valorAporte)
                _uiState.update { it.copy(mensagemFeedback = "Aporte registrado.") }
                carregarMetasFirestore()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erroOperacao = "Falha ao registrar aporte: ${e.localizedMessage}")
                }
            }
        }
    }

    fun excluirMetaFirestore(metaId: String) {
        viewModelScope.launch {
            try {
                firestoreRepository.excluir(metaId)
                _uiState.update { it.copy(mensagemFeedback = "Meta excluída.") }
                carregarMetasFirestore()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(erroOperacao = "Falha ao excluir meta: ${e.localizedMessage}")
                }
            }
        }
    }
}
