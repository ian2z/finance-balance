package br.com.financebalance.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.financebalance.mobile.data.firestore.MetaFinanceira
import br.com.financebalance.mobile.data.remote.ResumoDashboardDto
import br.com.financebalance.mobile.ui.components.*
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.util.*
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    mesSelecionado: String,
    carregando: Boolean,
    resumo: ResumoDashboardDto?,
    metasFirestore: List<MetaFinanceira>,
    layoutExpandido: Boolean,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit,
    onIrParaTransacoes: () -> Unit,
    onIrParaMetas: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho + Seletor de Mês (igual ao DashboardPage.tsx do Web)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeader(
                    titulo = "Dashboard",
                    descricao = "Resumo da sua situação financeira."
                )
                SeletorMesBar(
                    rotuloMes = nomeDoMes(mesSelecionado),
                    aoAnterior = onMesAnterior,
                    aoProximo = onProximoMes,
                    modifier = Modifier.fillMaxWidth()
                )
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

        // Indicadores Principais: Saldo Total, Receitas, Despesas, Resultado do Período
        item {
            val saldoTotal = resumo?.saldoTotal ?: BigDecimal.ZERO
            val receitas = resumo?.receitas ?: BigDecimal.ZERO
            val despesas = resumo?.despesas ?: BigDecimal.ZERO
            val resultado = resumo?.resultado ?: BigDecimal.ZERO
            val recPendentes = resumo?.receitasPendentes ?: BigDecimal.ZERO
            val despPendentes = resumo?.despesasPendentes ?: BigDecimal.ZERO

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card Destaque: Saldo Total
                FinanceCard(
                    borderColor = BorderStrong
                ) {
                    FieldLabel(texto = "SALDO TOTAL")
                    Text(
                        text = formatarMoeda(saldoTotal),
                        style = MaterialTheme.typography.displaySmall,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Somatório das contas ativas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Receitas e Despesas lado a lado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinanceCard(modifier = Modifier.weight(1f)) {
                        FieldLabel(texto = "RECEITAS")
                        DinheiroText(
                            valor = receitas,
                            tipo = "RECEITA",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (recPendentes > BigDecimal.ZERO)
                                "+ ${formatarMoeda(recPendentes)} a receber"
                            else
                                "Recebidas no mês",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    FinanceCard(modifier = Modifier.weight(1f)) {
                        FieldLabel(texto = "DESPESAS")
                        DinheiroText(
                            valor = despesas,
                            tipo = "DESPESA",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (despPendentes > BigDecimal.ZERO)
                                "+ ${formatarMoeda(despPendentes)} a pagar"
                            else
                                "Pagas no mês",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Resultado do Período e Orçamento do Mês
                if (layoutExpandido) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultadoCard(resultado = resultado, modifier = Modifier.weight(1f))
                        OrcamentoResumoCard(resumo = resumo, modifier = Modifier.weight(1f))
                    }
                } else {
                    ResultadoCard(resultado = resultado, modifier = Modifier.fillMaxWidth())
                    OrcamentoResumoCard(resumo = resumo, modifier = Modifier.fillMaxWidth())
                }
            }
        }

        // Despesas por Categoria e Maiores Gastos (com linhas divisórias limpas 1px, sem caixa dentro de caixa)
        item {
            if (layoutExpandido) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    DespesasPorCategoriaSection(
                        resumo = resumo,
                        modifier = Modifier.weight(1f)
                    )
                    MaioresGastosSection(
                        resumo = resumo,
                        onVerTransacoes = onIrParaTransacoes,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DespesasPorCategoriaSection(
                        resumo = resumo,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MaioresGastosSection(
                        resumo = resumo,
                        onVerTransacoes = onIrParaTransacoes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Resumo de Metas Financeiras (Cloud Firestore)
        item {
            val totalMetas = metasFirestore.size
            val concluidas = metasFirestore.count { it.concluida }
            val somaAtual = metasFirestore.sumOf { it.valorAtual }
            val somaAlvo = metasFirestore.sumOf { it.valorAlvo }
            val percentualGeral = if (somaAlvo > 0.0) ((somaAtual / somaAlvo) * 100.0).toFloat() else 0f

            FinanceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Metas financeiras",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    BotaoFantasma(
                        texto = "Ver metas",
                        onClick = onIrParaMetas
                    )
                }

                if (totalMetas == 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Nenhuma meta cadastrada. Defina objetivos e acompanhe a evolução do seu patrimônio.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formatarPercentual(percentualGeral),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    BarraProgresso(
                        percentual = percentualGeral,
                        inverterAlerta = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${formatarMoeda(somaAtual)} acumulados de ${formatarMoeda(somaAlvo)} ($concluidas de $totalMetas concluídas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultadoCard(
    resultado: BigDecimal,
    modifier: Modifier = Modifier
) {
    val negativo = resultado < BigDecimal.ZERO
    FinanceCard(modifier = modifier) {
        FieldLabel(texto = "RESULTADO DO PERÍODO")
        Text(
            text = "${if (negativo) "− " else ""}${formatarMoeda(resultado.abs())}",
            style = MaterialTheme.typography.headlineSmall,
            color = if (negativo) DespesaDanger else TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Receitas − despesas pagas",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun OrcamentoResumoCard(
    resumo: ResumoDashboardDto?,
    modifier: Modifier = Modifier
) {
    val orc = resumo?.orcamento
    FinanceCard(modifier = modifier) {
        FieldLabel(texto = "ORÇAMENTO DO MÊS")
        if (orc == null || orc.totalLimite <= BigDecimal.ZERO) {
            Text(
                text = "Sem limite definido",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Defina limites mensais por categoria para acompanhar o consumo.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        } else {
            Text(
                text = formatarPercentual(orc.percentual),
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            BarraProgresso(percentual = orc.percentual.toFloat())
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${formatarMoeda(orc.totalUtilizado)} de ${formatarMoeda(orc.totalLimite)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun DespesasPorCategoriaSection(
    resumo: ResumoDashboardDto?,
    modifier: Modifier = Modifier
) {
    FinanceCard(modifier = modifier) {
        Text(
            text = "Despesas por categoria",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(14.dp))

        val categorias = resumo?.despesasPorCategoria.orEmpty()
        if (categorias.isEmpty()) {
            Text(
                text = "Sem despesas pagas no período selecionado.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            categorias.forEachIndexed { index, cat ->
                val corCategoria = PaletaCategorias[index % PaletaCategorias.size]
                if (index > 0) {
                    HorizontalDivider(
                        color = BorderSubtle,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(corCategoria, RoundedCornerShape(3.dp))
                            )
                            Text(
                                text = cat.categoria,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "${formatarMoeda(cat.valor)} · ${formatarPercentual(cat.percentual)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(SurfaceContainerHigh)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth((cat.percentual.toFloat() / 100f).coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(999.dp))
                                .background(corCategoria)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaioresGastosSection(
    resumo: ResumoDashboardDto?,
    onVerTransacoes: () -> Unit,
    modifier: Modifier = Modifier
) {
    FinanceCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Maiores gastos",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            BotaoFantasma(
                texto = "Ver transações",
                onClick = onVerTransacoes
            )
        }
        Spacer(Modifier.height(8.dp))

        val gastos = resumo?.maioresGastos.orEmpty()
        if (gastos.isEmpty()) {
            Text(
                text = "Nenhum gasto registrado neste mês.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        } else {
            gastos.forEachIndexed { index, g ->
                if (index > 0) {
                    HorizontalDivider(color = BorderSubtle)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = g.descricao,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = buildString {
                                append(formatarDataBr(g.data))
                                if (!g.categoria.isNullOrBlank()) {
                                    append(" · ")
                                    append(g.categoria)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    DinheiroText(
                        valor = g.valor,
                        tipo = "DESPESA",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
