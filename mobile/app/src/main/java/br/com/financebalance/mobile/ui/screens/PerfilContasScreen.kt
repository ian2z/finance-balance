package br.com.financebalance.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.financebalance.mobile.data.local.UserSession
import br.com.financebalance.mobile.data.remote.ContaDto
import br.com.financebalance.mobile.ui.components.*
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.util.*
import java.math.BigDecimal

@Composable
fun PerfilContasScreen(
    sessao: UserSession,
    contas: List<ContaDto>,
    onCriarConta: (nome: String, tipo: String, saldoInicial: BigDecimal, onSucesso: () -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    var mostrarNovaConta by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho
        item {
            PageHeader(
                titulo = "Conta e perfil",
                descricao = "Suas informações pessoais e contas financeiras."
            )
        }

        // Card de Perfil do Usuário
        item {
            val inicial = (sessao.nome?.trim()?.firstOrNull() ?: 'U').uppercaseChar()
            FinanceCard(contentPadding = PaddingValues(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(OrangePrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = inicial.toString(),
                            color = OnOrangePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sessao.nome ?: "Usuário",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            text = sessao.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(Modifier.height(14.dp))

                FieldLabel(texto = "AMBIENTE CONECTADO")
                Text(
                    text = "API REST: ${sessao.apiUrl}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Cloud Firestore: metas_financeiras (usuário #${sessao.usuarioId})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(16.dp))
                BotaoSecundario(
                    texto = "Sair da conta",
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Cabeçalho de Contas Financeiras
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Contas financeiras",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "${contas.size} ${if (contas.size == 1) "conta cadastrada" else "contas cadastradas"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                BotaoPrimario(
                    texto = "Nova conta",
                    onClick = { mostrarNovaConta = true }
                )
            }
        }

        if (contas.isEmpty()) {
            item {
                EstadoVazio(
                    titulo = "Nenhuma conta cadastrada",
                    descricao = "Crie uma conta corrente, poupança ou carteira para registrar movimentações.",
                    acao = {
                        BotaoPrimario(
                            texto = "Nova conta",
                            onClick = { mostrarNovaConta = true }
                        )
                    }
                )
            }
        } else {
            items(contas, key = { it.id }) { conta ->
                FinanceCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conta.nome,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${rotuloTipoConta(conta.tipo)} · Saldo inicial: ${formatarMoeda(conta.saldoInicial)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Selo(
                            texto = if (conta.ativa) "ATIVA" else "INATIVA",
                            tom = if (conta.ativa) TomSelo.POSITIVO else TomSelo.NEUTRO
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo atual",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = formatarMoeda(conta.saldoAtual),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (conta.saldoAtual < BigDecimal.ZERO) DespesaDanger else TextPrimary
                        )
                    }
                }
            }
        }
    }

    if (mostrarNovaConta) {
        NovaContaDialog(
            onDismiss = { mostrarNovaConta = false },
            onConfirmar = { nome, tipo, saldoInicial ->
                onCriarConta(nome, tipo, saldoInicial) {
                    mostrarNovaConta = false
                }
            }
        )
    }
}

@Composable
private fun NovaContaDialog(
    onDismiss: () -> Unit,
    onConfirmar: (nome: String, tipo: String, saldoInicial: BigDecimal) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("CORRENTE") }
    var saldoTexto by remember { mutableStateOf("0,00") }

    val tipos = listOf(
        "CORRENTE" to "Conta corrente",
        "POUPANCA" to "Poupança",
        "CARTEIRA_DIGITAL" to "Carteira digital",
        "CARTEIRA" to "Carteira",
        "DINHEIRO" to "Dinheiro em espécie"
    )

    val saldo = lerValorBigDecimal(saldoTexto) ?: BigDecimal.ZERO

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLow,
        title = {
            Text(
                text = "Nova conta",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CampoFormulario(
                    rotulo = "NOME",
                    valor = nome,
                    onValorChange = { nome = it },
                    placeholder = "Ex.: Conta principal, Reserva"
                )
                Column {
                    FieldLabel(texto = "TIPO")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tipos.forEach { (cod, rotulo) ->
                            OpcaoFiltroChip(
                                selecionado = tipo == cod,
                                rotulo = rotulo,
                                onClick = { tipo = cod }
                            )
                        }
                    }
                }
                CampoFormulario(
                    rotulo = "SALDO INICIAL (R$)",
                    valor = saldoTexto,
                    onValorChange = { saldoTexto = it },
                    placeholder = "0,00",
                    teclado = KeyboardType.Decimal
                )
            }
        },
        confirmButton = {
            BotaoPrimario(
                texto = "Salvar",
                habilitado = nome.isNotBlank(),
                onClick = { if (nome.isNotBlank()) onConfirmar(nome, tipo, saldo) }
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
