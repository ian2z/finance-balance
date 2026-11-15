package br.com.financebalance.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.financebalance.mobile.data.remote.*
import br.com.financebalance.mobile.ui.components.*
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.util.*
import br.com.financebalance.mobile.viewmodel.FiltroTransacoesState
import java.math.BigDecimal

@Composable
fun TransacoesScreen(
    carregando: Boolean,
    transacoes: List<TransacaoDto>,
    totalElementos: Long,
    filtro: FiltroTransacoesState,
    contas: List<ContaDto>,
    categorias: List<CategoriaDto>,
    formasPagamento: List<RefDto>,
    onAtualizarFiltro: (FiltroTransacoesState) -> Unit,
    onRecarregar: () -> Unit,
    onSalvarTransacao: (idEdicao: Long?, request: TransacaoRequest, onSucesso: () -> Unit) -> Unit,
    onSalvarTransferencia: (request: TransferenciaRequest, onSucesso: () -> Unit) -> Unit,
    onExcluirTransacao: (Long) -> Unit
) {
    var mostrarModalTransacao by remember { mutableStateOf(false) }
    var mostrarModalTransferencia by remember { mutableStateOf(false) }
    var transacaoEmEdicao by remember { mutableStateOf<TransacaoDto?>(null) }
    var transacaoParaExcluir by remember { mutableStateOf<TransacaoDto?>(null) }
    var mostrarFiltrosAvancados by remember { mutableStateOf(false) }
    var textoBuscaLocal by remember(filtro.texto) { mutableStateOf(filtro.texto) }

    val temFiltroAtivo = filtro.texto.isNotBlank() ||
        filtro.tipo != null ||
        filtro.situacao != null ||
        filtro.categoriaId != null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho + Ações Principais (idêntico ao TransacoesPage.tsx do Web)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PageHeader(
                    titulo = "Transações",
                    descricao = "Receitas, despesas e transferências."
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BotaoSecundario(
                        texto = "Transferir",
                        onClick = { mostrarModalTransferencia = true },
                        modifier = Modifier.weight(1f)
                    )
                    BotaoPrimario(
                        texto = "Nova transação",
                        onClick = {
                            transacaoEmEdicao = null
                            mostrarModalTransacao = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Painel de Busca e Filtros
        item {
            FinanceCard(contentPadding = PaddingValues(16.dp)) {
                CampoFormulario(
                    rotulo = "BUSCAR",
                    valor = textoBuscaLocal,
                    onValorChange = {
                        textoBuscaLocal = it
                        onAtualizarFiltro(filtro.copy(texto = it))
                    },
                    placeholder = "Descrição da movimentação…",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (textoBuscaLocal.isNotBlank()) {
                            IconButton(onClick = {
                                textoBuscaLocal = ""
                                onAtualizarFiltro(filtro.copy(texto = ""))
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = TextSecondary)
                            }
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(texto = "TIPO E SITUAÇÃO")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpcaoFiltroChip(
                        selecionado = filtro.tipo == null && filtro.situacao == null,
                        rotulo = "Todos",
                        onClick = { onAtualizarFiltro(filtro.copy(tipo = null, situacao = null)) }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.tipo == "RECEITA",
                        rotulo = "Receitas",
                        onClick = {
                            val novo = if (filtro.tipo == "RECEITA") null else "RECEITA"
                            onAtualizarFiltro(filtro.copy(tipo = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.tipo == "DESPESA",
                        rotulo = "Despesas",
                        onClick = {
                            val novo = if (filtro.tipo == "DESPESA") null else "DESPESA"
                            onAtualizarFiltro(filtro.copy(tipo = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.tipo == "TRANSFERENCIA",
                        rotulo = "Transferências",
                        onClick = {
                            val novo = if (filtro.tipo == "TRANSFERENCIA") null else "TRANSFERENCIA"
                            onAtualizarFiltro(filtro.copy(tipo = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.situacao == "PENDENTE",
                        rotulo = "Pendentes",
                        onClick = {
                            val novo = if (filtro.situacao == "PENDENTE") null else "PENDENTE"
                            onAtualizarFiltro(filtro.copy(situacao = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.situacao == "PAGA",
                        rotulo = "Pagas",
                        onClick = {
                            val novo = if (filtro.situacao == "PAGA") null else "PAGA"
                            onAtualizarFiltro(filtro.copy(situacao = novo))
                        }
                    )
                }

                // Filtro por Categoria expansível
                if (categorias.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BotaoFantasma(
                            texto = if (mostrarFiltrosAvancados) "Ocultar categorias" else "Filtrar por categoria",
                            onClick = { mostrarFiltrosAvancados = !mostrarFiltrosAvancados }
                        )
                        if (temFiltroAtivo) {
                            BotaoFantasma(
                                texto = "Limpar filtros",
                                corTexto = CreamTertiary,
                                onClick = {
                                    textoBuscaLocal = ""
                                    onAtualizarFiltro(FiltroTransacoesState())
                                }
                            )
                        }
                    }

                    AnimatedVisibility(visible = mostrarFiltrosAvancados || filtro.categoriaId != null) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            FieldLabel(texto = "CATEGORIA")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OpcaoFiltroChip(
                                    selecionado = filtro.categoriaId == null,
                                    rotulo = "Todas",
                                    onClick = { onAtualizarFiltro(filtro.copy(categoriaId = null)) }
                                )
                                categorias.forEach { cat ->
                                    OpcaoFiltroChip(
                                        selecionado = filtro.categoriaId == cat.id,
                                        rotulo = cat.nome,
                                        onClick = {
                                            val novoId = if (filtro.categoriaId == cat.id) null else cat.id
                                            onAtualizarFiltro(filtro.copy(categoriaId = novoId))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (carregando) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = OrangePrimary,
                    trackColor = SurfaceContainerHigh
                )
            }
        }

        // Contagem de resultados
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$totalElementos ${if (totalElementos == 1L) "resultado" else "resultados"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                BotaoFantasma(
                    texto = "Atualizar",
                    onClick = onRecarregar
                )
            }
        }

        if (!carregando && transacoes.isEmpty()) {
            item {
                EstadoVazio(
                    titulo = if (temFiltroAtivo) "Nenhuma transação encontrada" else "Nenhuma transação ainda",
                    descricao = if (temFiltroAtivo) "Ajuste ou limpe os filtros acima." else "Registre sua primeira receita ou despesa.",
                    acao = if (!temFiltroAtivo) {
                        {
                            BotaoPrimario(
                                texto = "Nova transação",
                                onClick = {
                                    transacaoEmEdicao = null
                                    mostrarModalTransacao = true
                                }
                            )
                        }
                    } else null
                )
            }
        } else {
            items(transacoes, key = { it.id }) { tx ->
                TransacaoItemCard(
                    tx = tx,
                    onEditar = {
                        if (tx.tipo != "TRANSFERENCIA") {
                            transacaoEmEdicao = tx
                            mostrarModalTransacao = true
                        }
                    },
                    onExcluir = { transacaoParaExcluir = tx }
                )
            }
        }
    }

    // Modal de Nova / Editar Transação
    if (mostrarModalTransacao) {
        TransacaoFormDialog(
            inicial = transacaoEmEdicao,
            contas = contas,
            categorias = categorias,
            formasPagamento = formasPagamento,
            onDismiss = { mostrarModalTransacao = false },
            onConfirmar = { req ->
                onSalvarTransacao(transacaoEmEdicao?.id, req) {
                    mostrarModalTransacao = false
                }
            }
        )
    }

    // Modal de Transferência entre Contas
    if (mostrarModalTransferencia) {
        TransferenciaFormDialog(
            contas = contas,
            onDismiss = { mostrarModalTransferencia = false },
            onConfirmar = { req ->
                onSalvarTransferencia(req) {
                    mostrarModalTransferencia = false
                }
            }
        )
    }

    // Confirmação de Exclusão
    transacaoParaExcluir?.let { tx ->
        AlertDialog(
            onDismissRequest = { transacaoParaExcluir = null },
            containerColor = SurfaceContainerLow,
            title = {
                Text(
                    text = "Excluir transação",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Excluir \"${tx.descricao}\" (${formatarMoeda(tx.valor)})? Esta ação não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                BotaoPrimario(
                    texto = "Excluir",
                    onClick = {
                        onExcluirTransacao(tx.id)
                        transacaoParaExcluir = null
                    }
                )
            },
            dismissButton = {
                BotaoSecundario(
                    texto = "Cancelar",
                    onClick = { transacaoParaExcluir = null }
                )
            }
        )
    }
}

@Composable
private fun TransacaoItemCard(
    tx: TransacaoDto,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val rotuloConta = if (tx.contaDestino != null) {
        "${tx.conta.nome} → ${tx.contaDestino.nome}"
    } else {
        tx.conta.nome
    }

    FinanceCard(contentPadding = PaddingValues(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.descricao,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${formatarDataBr(tx.data)} · ${tx.categoria?.nome ?: "Transferência"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = rotuloConta,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.width(12.dp))
            DinheiroText(
                valor = tx.valor,
                tipo = tx.tipo,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (tx.situacao == "PENDENTE") {
                    Selo(texto = "PENDENTE", tom = TomSelo.ALERTA)
                }
                tx.etiquetas.forEach { etq ->
                    Selo(texto = etq.nome, tom = TomSelo.NEUTRO)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (tx.tipo != "TRANSFERENCIA") {
                    BotaoFantasma(
                        texto = "Editar",
                        corTexto = OrangePrimary,
                        onClick = onEditar
                    )
                }
                BotaoFantasma(
                    texto = "Excluir",
                    corTexto = DespesaDanger,
                    onClick = onExcluir
                )
            }
        }
    }
}

@Composable
private fun TransacaoFormDialog(
    inicial: TransacaoDto?,
    contas: List<ContaDto>,
    categorias: List<CategoriaDto>,
    formasPagamento: List<RefDto>,
    onDismiss: () -> Unit,
    onConfirmar: (TransacaoRequest) -> Unit
) {
    var tipo by remember { mutableStateOf(inicial?.tipo ?: "DESPESA") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var valorTexto by remember { mutableStateOf(valorParaCampoBr(inicial?.valor)) }
    var dataBr by remember { mutableStateOf(inicial?.data?.let { formatarDataBr(it) } ?: hojeBr()) }
    var situacao by remember { mutableStateOf(inicial?.situacao ?: "PAGA") }
    var observacao by remember { mutableStateOf(inicial?.observacao ?: "") }

    val categoriasDoTipo = remember(tipo, categorias) {
        categorias.filter { it.tipo == tipo }
    }

    var categoriaId by remember(tipo, categoriasDoTipo) {
        mutableStateOf(
            inicial?.categoria?.id?.takeIf { id -> categoriasDoTipo.any { it.id == id } }
                ?: categoriasDoTipo.firstOrNull()?.id
        )
    }
    var contaId by remember(contas) {
        mutableStateOf(inicial?.conta?.id ?: contas.firstOrNull()?.id)
    }
    var formaPagamentoId by remember(formasPagamento) {
        mutableStateOf(inicial?.formaPagamento?.id ?: formasPagamento.firstOrNull()?.id)
    }

    val valorNumerico = lerValorBigDecimal(valorTexto)
    val dataIsoValida = converterDataBrParaIso(dataBr)
    val podeSalvar = descricao.isNotBlank() &&
        valorNumerico != null &&
        valorNumerico > BigDecimal.ZERO &&
        dataIsoValida != null &&
        categoriaId != null &&
        contaId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = {
            Text(
                text = if (inicial == null) "Nova transação" else "Editar transação",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (contas.isEmpty()) {
                    Text(
                        text = "Cadastre pelo menos uma conta na aba Conta antes de registrar transações.",
                        color = AmberSoft,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Tipo
                Column {
                    FieldLabel(texto = "TIPO")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcaoFiltroChip(
                            selecionado = tipo == "DESPESA",
                            rotulo = "Despesa",
                            onClick = { tipo = "DESPESA" },
                            modifier = Modifier.weight(1f)
                        )
                        OpcaoFiltroChip(
                            selecionado = tipo == "RECEITA",
                            rotulo = "Receita",
                            onClick = { tipo = "RECEITA" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                CampoFormulario(
                    rotulo = "DESCRIÇÃO",
                    valor = descricao,
                    onValorChange = { descricao = it },
                    placeholder = "Ex.: Supermercado, Aluguel, Salário"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoFormulario(
                        rotulo = "VALOR (R$)",
                        valor = valorTexto,
                        onValorChange = { valorTexto = it },
                        placeholder = "0,00",
                        teclado = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    CampoDataBr(
                        rotulo = "DATA",
                        valorBr = dataBr,
                        onValorBrChange = { dataBr = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Situação
                Column {
                    FieldLabel(texto = "SITUAÇÃO")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcaoFiltroChip(
                            selecionado = situacao == "PAGA",
                            rotulo = "Paga",
                            onClick = { situacao = "PAGA" },
                            modifier = Modifier.weight(1f)
                        )
                        OpcaoFiltroChip(
                            selecionado = situacao == "PENDENTE",
                            rotulo = "Pendente",
                            onClick = { situacao = "PENDENTE" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Conta
                Column {
                    FieldLabel(texto = "CONTA")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contas.forEach { c ->
                            OpcaoFiltroChip(
                                selecionado = contaId == c.id,
                                rotulo = c.nome,
                                onClick = { contaId = c.id }
                            )
                        }
                    }
                }

                // Categoria
                Column {
                    FieldLabel(texto = "CATEGORIA")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriasDoTipo.forEach { cat ->
                            OpcaoFiltroChip(
                                selecionado = categoriaId == cat.id,
                                rotulo = cat.nome,
                                onClick = { categoriaId = cat.id }
                            )
                        }
                    }
                }

                // Forma de pagamento
                if (formasPagamento.isNotEmpty()) {
                    Column {
                        FieldLabel(texto = "FORMA DE PAGAMENTO")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            formasPagamento.forEach { fp ->
                                OpcaoFiltroChip(
                                    selecionado = formaPagamentoId == fp.id,
                                    rotulo = fp.nome,
                                    onClick = { formaPagamentoId = fp.id }
                                )
                            }
                        }
                    }
                }

                CampoFormulario(
                    rotulo = "OBSERVAÇÃO (OPCIONAL)",
                    valor = observacao,
                    onValorChange = { observacao = it },
                    placeholder = "Detalhes adicionais…"
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = "Salvar",
                habilitado = podeSalvar,
                onClick = {
                    if (podeSalvar) {
                        onConfirmar(
                            TransacaoRequest(
                                descricao = descricao.trim(),
                                valor = valorNumerico!!,
                                tipo = tipo,
                                data = dataIsoValida!!,
                                dataVencimento = if (situacao == "PENDENTE") dataIsoValida else null,
                                situacao = situacao,
                                categoriaId = categoriaId!!,
                                contaId = contaId!!,
                                formaPagamentoId = formaPagamentoId,
                                observacao = observacao.trim().ifBlank { null }
                            )
                        )
                    }
                }
            )
        },
        dismissButton = {
            BotaoSecundario(
                texto = "Cancelar",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun TransferenciaFormDialog(
    contas: List<ContaDto>,
    onDismiss: () -> Unit,
    onConfirmar: (TransferenciaRequest) -> Unit
) {
    var descricao by remember { mutableStateOf("Transferência entre contas") }
    var valorTexto by remember { mutableStateOf("") }
    var dataBr by remember { mutableStateOf(hojeBr()) }
    var contaOrigemId by remember(contas) { mutableStateOf(contas.firstOrNull()?.id) }
    var contaDestinoId by remember(contas) { mutableStateOf(contas.drop(1).firstOrNull()?.id ?: contas.firstOrNull()?.id) }
    var observacao by remember { mutableStateOf("") }

    val valorNumerico = lerValorBigDecimal(valorTexto)
    val dataIsoValida = converterDataBrParaIso(dataBr)
    val podeTransferir = descricao.isNotBlank() &&
        valorNumerico != null &&
        valorNumerico > BigDecimal.ZERO &&
        dataIsoValida != null &&
        contaOrigemId != null &&
        contaDestinoId != null &&
        contaOrigemId != contaDestinoId

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = {
            Text(
                text = "Transferir entre contas",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CampoFormulario(
                    rotulo = "DESCRIÇÃO",
                    valor = descricao,
                    onValorChange = { descricao = it },
                    placeholder = "Ex.: Reserva mensal"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoFormulario(
                        rotulo = "VALOR (R$)",
                        valor = valorTexto,
                        onValorChange = { valorTexto = it },
                        placeholder = "0,00",
                        teclado = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    CampoDataBr(
                        rotulo = "DATA",
                        valorBr = dataBr,
                        onValorBrChange = { dataBr = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Column {
                    FieldLabel(texto = "CONTA DE ORIGEM")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contas.forEach { c ->
                            OpcaoFiltroChip(
                                selecionado = contaOrigemId == c.id,
                                rotulo = c.nome,
                                onClick = { contaOrigemId = c.id }
                            )
                        }
                    }
                }

                Column {
                    FieldLabel(texto = "CONTA DE DESTINO")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contas.forEach { c ->
                            OpcaoFiltroChip(
                                selecionado = contaDestinoId == c.id,
                                rotulo = c.nome,
                                onClick = { contaDestinoId = c.id }
                            )
                        }
                    }
                }

                if (contaOrigemId != null && contaOrigemId == contaDestinoId) {
                    Text(
                        text = "Selecione contas diferentes para origem e destino.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorCoral
                    )
                }

                CampoFormulario(
                    rotulo = "OBSERVAÇÃO (OPCIONAL)",
                    valor = observacao,
                    onValorChange = { observacao = it },
                    placeholder = "Opcional…"
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = "Transferir",
                habilitado = podeTransferir,
                onClick = {
                    if (podeTransferir) {
                        onConfirmar(
                            TransferenciaRequest(
                                descricao = descricao.trim(),
                                valor = valorNumerico!!,
                                data = dataIsoValida!!,
                                contaOrigemId = contaOrigemId!!,
                                contaDestinoId = contaDestinoId!!,
                                observacao = observacao.trim().ifBlank { null }
                            )
                        )
                    }
                }
            )
        },
        dismissButton = {
            BotaoSecundario(
                texto = "Cancelar",
                onClick = onDismiss
            )
        }
    )
}
