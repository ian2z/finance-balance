package br.com.financebalance.mobile.ui.screens

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.financebalance.mobile.data.firestore.MetaFinanceira
import br.com.financebalance.mobile.ui.components.*
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.util.*
import br.com.financebalance.mobile.viewmodel.FiltroMetasState

@Composable
fun MetasFirestoreScreen(
    carregando: Boolean,
    metas: List<MetaFinanceira>,
    filtro: FiltroMetasState,
    onAtualizarFiltro: (FiltroMetasState) -> Unit,
    onRecarregar: () -> Unit,
    onSalvarMeta: (MetaFinanceira, onSucesso: () -> Unit) -> Unit,
    onAdicionarAporte: (MetaFinanceira, Double) -> Unit,
    onExcluirMeta: (String) -> Unit
) {
    var mostrarDialogMeta by remember { mutableStateOf(false) }
    var metaEmEdicao by remember { mutableStateOf<MetaFinanceira?>(null) }
    var metaParaAporte by remember { mutableStateOf<MetaFinanceira?>(null) }
    var metaParaExcluir by remember { mutableStateOf<MetaFinanceira?>(null) }

    val temFiltroAtivo = filtro.texto.isNotBlank() ||
        filtro.status != "TODAS" ||
        filtro.prioridade != null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabeçalho + Ação Principal
        item {
            PageHeader(
                titulo = "Metas financeiras",
                descricao = "Objetivos e reservas sincronizados na nuvem.",
                acaoDireita = {
                    BotaoPrimario(
                        texto = "Nova meta",
                        onClick = {
                            metaEmEdicao = null
                            mostrarDialogMeta = true
                        }
                    )
                }
            )
        }

        // Painel de Busca e Filtros
        item {
            FinanceCard(contentPadding = PaddingValues(16.dp)) {
                CampoFormulario(
                    rotulo = "BUSCAR",
                    valor = filtro.texto,
                    onValorChange = { onAtualizarFiltro(filtro.copy(texto = it)) },
                    placeholder = "Título, categoria ou observação…",
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (filtro.texto.isNotBlank()) {
                            IconButton(onClick = { onAtualizarFiltro(filtro.copy(texto = "")) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = TextSecondary)
                            }
                        }
                    }
                )

                Spacer(Modifier.height(14.dp))
                FieldLabel(texto = "SITUAÇÃO E PRIORIDADE")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OpcaoFiltroChip(
                        selecionado = filtro.status == "TODAS" && filtro.prioridade == null,
                        rotulo = "Todas",
                        onClick = { onAtualizarFiltro(filtro.copy(status = "TODAS", prioridade = null)) }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.status == "EM_ANDAMENTO",
                        rotulo = "Em andamento",
                        onClick = {
                            val novo = if (filtro.status == "EM_ANDAMENTO") "TODAS" else "EM_ANDAMENTO"
                            onAtualizarFiltro(filtro.copy(status = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.status == "CONCLUIDAS",
                        rotulo = "Concluídas",
                        onClick = {
                            val novo = if (filtro.status == "CONCLUIDAS") "TODAS" else "CONCLUIDAS"
                            onAtualizarFiltro(filtro.copy(status = novo))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.prioridade == "ALTA",
                        rotulo = "Prioridade alta",
                        onClick = {
                            val nova = if (filtro.prioridade == "ALTA") null else "ALTA"
                            onAtualizarFiltro(filtro.copy(prioridade = nova))
                        }
                    )
                    OpcaoFiltroChip(
                        selecionado = filtro.prioridade == "MEDIA",
                        rotulo = "Prioridade média",
                        onClick = {
                            val nova = if (filtro.prioridade == "MEDIA") null else "MEDIA"
                            onAtualizarFiltro(filtro.copy(prioridade = nova))
                        }
                    )
                }

                if (temFiltroAtivo) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        BotaoFantasma(
                            texto = "Limpar filtros",
                            corTexto = CreamTertiary,
                            onClick = { onAtualizarFiltro(FiltroMetasState()) }
                        )
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

        // Contagem + Atualizar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${metas.size} ${if (metas.size == 1) "meta" else "metas"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                BotaoFantasma(
                    texto = "Atualizar",
                    onClick = onRecarregar
                )
            }
        }

        if (!carregando && metas.isEmpty()) {
            item {
                EstadoVazio(
                    titulo = if (temFiltroAtivo) "Nenhuma meta encontrada" else "Nenhuma meta cadastrada",
                    descricao = if (temFiltroAtivo)
                        "Ajuste ou limpe os filtros acima."
                    else
                        "Defina um objetivo financeiro com valor alvo e prazo para acompanhar seus aportes.",
                    acao = if (!temFiltroAtivo) {
                        {
                            BotaoPrimario(
                                texto = "Nova meta",
                                onClick = {
                                    metaEmEdicao = null
                                    mostrarDialogMeta = true
                                }
                            )
                        }
                    } else null
                )
            }
        } else {
            items(metas, key = { it.id }) { meta ->
                MetaFirestoreCard(
                    meta = meta,
                    onAporte = { metaParaAporte = meta },
                    onEditar = {
                        metaEmEdicao = meta
                        mostrarDialogMeta = true
                    },
                    onExcluir = { metaParaExcluir = meta }
                )
            }
        }
    }

    // Modal de Criar / Editar Meta
    if (mostrarDialogMeta) {
        MetaFormDialog(
            inicial = metaEmEdicao,
            onDismiss = { mostrarDialogMeta = false },
            onConfirmar = { metaEditada ->
                onSalvarMeta(metaEditada) {
                    mostrarDialogMeta = false
                }
            }
        )
    }

    // Modal de Aporte Rápido
    metaParaAporte?.let { meta ->
        AporteDialog(
            meta = meta,
            onDismiss = { metaParaAporte = null },
            onConfirmar = { valor ->
                onAdicionarAporte(meta, valor)
                metaParaAporte = null
            }
        )
    }

    // Confirmação de Exclusão
    metaParaExcluir?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaParaExcluir = null },
            containerColor = SurfaceContainerLow,
            title = {
                Text(
                    text = "Excluir meta",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Excluir \"${meta.titulo}\"? Esta ação removerá o registro da nuvem e não pode ser desfeita.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                BotaoPrimario(
                    texto = "Excluir",
                    onClick = {
                        onExcluirMeta(meta.id)
                        metaParaExcluir = null
                    }
                )
            },
            dismissButton = {
                BotaoSecundario(
                    texto = "Cancelar",
                    onClick = { metaParaExcluir = null }
                )
            }
        )
    }
}

@Composable
private fun MetaFirestoreCard(
    meta: MetaFinanceira,
    onAporte: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    val tomPrioridade = when (meta.prioridade) {
        "ALTA" -> TomSelo.ALERTA
        "BAIXA" -> TomSelo.NEUTRO
        else -> TomSelo.POSITIVO
    }

    FinanceCard(contentPadding = PaddingValues(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meta.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = buildString {
                        append(meta.categoria)
                        if (meta.prazo.isNotBlank()) {
                            append(" · Prazo: ")
                            append(formatarDataBr(meta.prazo))
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Spacer(Modifier.width(10.dp))
            Selo(
                texto = if (meta.concluida) "CONCLUÍDA" else rotuloPrioridade(meta.prioridade).uppercase(),
                tom = if (meta.concluida) TomSelo.POSITIVO else tomPrioridade
            )
        }

        if (meta.observacao.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = meta.observacao,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${formatarMoeda(meta.valorAtual)} de ${formatarMoeda(meta.valorAlvo)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary
            )
            Text(
                text = formatarPercentual(meta.percentualConcluido),
                style = MaterialTheme.typography.labelLarge,
                color = if (meta.concluida) AmberSecondary else CreamTertiary
            )
        }

        Spacer(Modifier.height(8.dp))
        BarraProgresso(
            percentual = meta.percentualConcluido,
            inverterAlerta = true
        )
        Spacer(Modifier.height(6.dp))

        Text(
            text = if (meta.concluida)
                "Objetivo alcançado"
            else
                "Restante: ${formatarMoeda(meta.valorRestante)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = BorderSubtle)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotaoSecundario(
                texto = "Registrar aporte",
                onClick = onAporte
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                BotaoFantasma(
                    texto = "Editar",
                    corTexto = OrangePrimary,
                    onClick = onEditar
                )
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
private fun MetaFormDialog(
    inicial: MetaFinanceira?,
    onDismiss: () -> Unit,
    onConfirmar: (MetaFinanceira) -> Unit
) {
    var titulo by remember { mutableStateOf(inicial?.titulo ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: "Reserva de emergência") }
    var valorAlvoTexto by remember { mutableStateOf(valorParaCampoBr(inicial?.valorAlvo)) }
    var valorAtualTexto by remember {
        mutableStateOf(
            if (inicial != null && inicial.valorAtual > 0.0) valorParaCampoBr(inicial.valorAtual) else "0,00"
        )
    }
    var prazoBr by remember {
        mutableStateOf(
            if (!inicial?.prazo.isNullOrBlank()) formatarDataBr(inicial?.prazo) else dataFuturaBr(6)
        )
    }
    var prioridade by remember { mutableStateOf(inicial?.prioridade ?: "MEDIA") }
    var observacao by remember { mutableStateOf(inicial?.observacao ?: "") }

    val categoriasSugeridas = listOf(
        "Reserva de emergência",
        "Viagem",
        "Educação",
        "Investimento",
        "Patrimônio"
    )

    val alvo = lerValorDouble(valorAlvoTexto) ?: 0.0
    val atual = lerValorDouble(valorAtualTexto) ?: 0.0
    val prazoIso = converterDataBrParaIso(prazoBr)
    val valido = titulo.isNotBlank() && alvo > 0.0 && atual >= 0.0 && prazoIso != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = {
            Text(
                text = if (inicial == null) "Nova meta financeira" else "Editar meta financeira",
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
                    rotulo = "TÍTULO",
                    valor = titulo,
                    onValorChange = { titulo = it },
                    placeholder = "Ex.: Reserva de 6 meses, Intercâmbio"
                )

                Column {
                    FieldLabel(texto = "CATEGORIA")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoriasSugeridas.forEach { cat ->
                            OpcaoFiltroChip(
                                selecionado = categoria == cat,
                                rotulo = cat,
                                onClick = { categoria = cat }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampoFormulario(
                        rotulo = "VALOR ALVO (R$)",
                        valor = valorAlvoTexto,
                        onValorChange = { valorAlvoTexto = it },
                        placeholder = "5.000,00",
                        teclado = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    CampoFormulario(
                        rotulo = "ACUMULADO (R$)",
                        valor = valorAtualTexto,
                        onValorChange = { valorAtualTexto = it },
                        placeholder = "0,00",
                        teclado = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }

                CampoDataBr(
                    rotulo = "PRAZO DESEJADO",
                    valorBr = prazoBr,
                    onValorBrChange = { prazoBr = it }
                )

                Column {
                    FieldLabel(texto = "PRIORIDADE")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALTA" to "Alta", "MEDIA" to "Média", "BAIXA" to "Baixa").forEach { (cod, rotulo) ->
                            OpcaoFiltroChip(
                                selecionado = prioridade == cod,
                                rotulo = rotulo,
                                onClick = { prioridade = cod },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                CampoFormulario(
                    rotulo = "OBSERVAÇÃO (OPCIONAL)",
                    valor = observacao,
                    onValorChange = { observacao = it },
                    placeholder = "Ex.: Aplicado em Tesouro Selic…"
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = "Salvar",
                habilitado = valido,
                onClick = {
                    if (valido) {
                        onConfirmar(
                            MetaFinanceira(
                                id = inicial?.id ?: "",
                                titulo = titulo.trim(),
                                categoria = categoria.trim(),
                                valorAlvo = alvo,
                                valorAtual = atual,
                                prazo = prazoIso!!,
                                prioridade = prioridade,
                                observacao = observacao.trim(),
                                criadoEm = inicial?.criadoEm ?: System.currentTimeMillis()
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
private fun AporteDialog(
    meta: MetaFinanceira,
    onDismiss: () -> Unit,
    onConfirmar: (Double) -> Unit
) {
    var valorTexto by remember { mutableStateOf("100,00") }
    val valor = lerValorDouble(valorTexto) ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = {
            Text(
                text = "Registrar aporte",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${meta.titulo} · Acumulado: ${formatarMoeda(meta.valorAtual)} de ${formatarMoeda(meta.valorAlvo)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                CampoFormulario(
                    rotulo = "VALOR DO APORTE (R$)",
                    valor = valorTexto,
                    onValorChange = { valorTexto = it },
                    placeholder = "0,00",
                    teclado = KeyboardType.Decimal
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = "Confirmar aporte",
                habilitado = valor > 0.0,
                onClick = { if (valor > 0.0) onConfirmar(valor) }
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
