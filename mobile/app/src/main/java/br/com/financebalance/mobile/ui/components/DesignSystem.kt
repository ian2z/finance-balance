package br.com.financebalance.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.financebalance.mobile.ui.theme.*
import br.com.financebalance.mobile.util.*
import java.math.BigDecimal

/**
 * Cabeçalho de página unificado (espelha PageHeader de web/src/components/ui.tsx).
 */
@Composable
fun PageHeader(
    titulo: String,
    descricao: String? = null,
    acaoDireita: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            if (!descricao.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        if (acaoDireita != null) {
            Spacer(Modifier.width(12.dp))
            acaoDireita()
        }
    }
}

/**
 * Card estrutural único do sistema: superfície #18181B, borda 1.dp #27272A, raio 16.dp, sem sombra difusa.
 */
@Composable
fun FinanceCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderSubtle,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLow,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Rótulo superior de indicador ou campo (12.sp, semibold, tom creme #FEF3C7).
 */
@Composable
fun FieldLabel(
    texto: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium,
        color = CreamTertiary,
        modifier = modifier.padding(bottom = 6.dp)
    )
}

/**
 * Exibição de valor monetário com algarismos tabulares e sinal explícito (+ / −).
 * Segue exatamente o componente Dinheiro do Web:
 * - RECEITA: âmbar (#F59E0B) com "+ "
 * - DESPESA: coral (#FFB4AB) com "− "
 * - TRANSFERENCIA / Neutro: branco (#FAFAFA)
 */
@Composable
fun DinheiroText(
    valor: BigDecimal?,
    tipo: String? = null,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    modifier: Modifier = Modifier
) {
    val v = (valor ?: BigDecimal.ZERO).abs()
    val (cor, sinal) = when (tipo) {
        "RECEITA" -> ReceitaAmber to "+ "
        "DESPESA" -> DespesaDanger to "− "
        "TRANSFERENCIA" -> TextPrimary to "⇄ "
        else -> TextPrimary to ""
    }
    Text(
        text = "$sinal${formatarMoeda(v)}",
        style = style.copy(
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = "tnum, lnum"
        ),
        color = cor,
        modifier = modifier
    )
}

/**
 * Selo (Badge) idêntico ao Selo do Web (neutro, positivo, alerta, critico).
 */
enum class TomSelo { NEUTRO, POSITIVO, ALERTA, CRITICO }

@Composable
fun Selo(
    texto: String,
    tom: TomSelo = TomSelo.NEUTRO,
    modifier: Modifier = Modifier
) {
    val (bg, borda, corTexto) = when (tom) {
        TomSelo.NEUTRO -> Triple(SurfaceContainerHigh, BorderStrong, CreamTertiary)
        TomSelo.POSITIVO -> Triple(SurfaceContainerLow, AmberSecondary, AmberSecondary)
        TomSelo.ALERTA -> Triple(SurfaceContainerLow, AmberSoft, AmberSoft)
        TomSelo.CRITICO -> Triple(SurfaceContainerHigh, OrangeHover, OrangeHover)
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = corTexto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/**
 * Barra de progresso limpa e sem brilho, com semântica de cores por percentual.
 */
@Composable
fun BarraProgresso(
    percentual: Float,
    inverterAlerta: Boolean = false,
    modifier: Modifier = Modifier
) {
    val p = (percentual / 100f).coerceIn(0f, 1f)
    val cor = if (inverterAlerta) {
        // Para metas: quanto mais perto de 100%, mais positivo (âmbar dourado)
        if (percentual >= 100f) AmberSecondary else OrangePrimary
    } else {
        // Para orçamento: >= 100% alerta laranja escuro, >= 80% âmbar, < 80% laranja
        when {
            percentual >= 100f -> OrangeHover
            percentual >= 80f -> AmberSoft
            else -> OrangePrimary
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(SurfaceContainerHigh)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(p)
                .clip(RoundedCornerShape(999.dp))
                .background(cor)
        )
    }
}

/**
 * Seletor de mês (‹ Novembro de 2026 ›) idêntico ao SeletorMes de web/src/components/ui.tsx.
 */
@Composable
fun SeletorMesBar(
    rotuloMes: String,
    aoAnterior: () -> Unit,
    aoProximo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceContainerHigh,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderStrong)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = aoAnterior,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Mês anterior",
                    tint = OrangePrimary
                )
            }
            Text(
                text = rotuloMes,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(min = 136.dp)
            )
            IconButton(
                onClick = aoProximo,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Próximo mês",
                    tint = OrangePrimary
                )
            }
        }
    }
}

/**
 * Campo de texto com rótulo visível acima do input (house rule: labels you can see above the field).
 */
@Composable
fun CampoFormulario(
    rotulo: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (rotulo.isNotBlank()) {
            FieldLabel(texto = rotulo)
        }
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            placeholder = if (placeholder.isNotBlank()) {
                { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceContainerHigh,
                unfocusedContainerColor = SurfaceContainerHigh,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = BorderStrong,
                cursorColor = OrangePrimary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Campo de data brasileiro (DD/MM/AAAA) com digitação formatada e seletor de calendário nativo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoDataBr(
    rotulo: String,
    valorBr: String,
    onValorBrChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "DD/MM/AAAA"
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    CampoFormulario(
        rotulo = rotulo,
        valor = valorBr,
        onValorChange = { digitado ->
            onValorBrChange(aplicarMascaraDataBr(digitado))
        },
        placeholder = placeholder,
        teclado = KeyboardType.Number,
        trailingIcon = {
            IconButton(onClick = { mostrarCalendario = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Selecionar data no calendário",
                    tint = CreamTertiary
                )
            }
        },
        modifier = modifier
    )

    if (mostrarCalendario) {
        val millisIniciais = remember(valorBr) {
            isoParaEpochMillisUtc(valorBr) ?: System.currentTimeMillis()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millisIniciais)

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                BotaoPrimario(
                    texto = "Confirmar",
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onValorBrChange(epochMillisUtcParaDataBr(millis))
                        }
                        mostrarCalendario = false
                    }
                )
            },
            dismissButton = {
                BotaoFantasma(
                    texto = "Cancelar",
                    onClick = { mostrarCalendario = false }
                )
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainerLow)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = SurfaceContainerLow,
                    titleContentColor = CreamTertiary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = CreamTertiary,
                    dayContentColor = TextPrimary,
                    selectedDayContainerColor = OrangePrimary,
                    selectedDayContentColor = OnOrangePrimary,
                    todayDateBorderColor = OrangePrimary,
                    todayContentColor = OrangePrimary
                )
            )
        }
    }
}

// ==================== SISTEMA ÚNICO DE BOTÕES ====================

@Composable
fun BotaoPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    icone: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !carregando,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangePrimary,
            contentColor = OnOrangePrimary,
            disabledContainerColor = SurfaceContainerHigh,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        if (carregando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = OnOrangePrimary
            )
        } else {
            if (icone != null) {
                icone()
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = if (habilitado) OnOrangePrimary else TextMuted
            )
        }
    }
}

@Composable
fun BotaoSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icone: (@Composable () -> Unit)? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.heightIn(min = 44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfaceContainerHigh,
            contentColor = CreamTertiary,
            disabledContainerColor = SurfaceContainerLow,
            disabledContentColor = TextMuted
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (icone != null) {
            icone()
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (habilitado) CreamTertiary else TextMuted
        )
    }
}

@Composable
fun BotaoFantasma(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    corTexto: Color = OrangePrimary
) {
    TextButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.heightIn(min = 40.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (habilitado) corTexto else TextMuted
        )
    }
}

/**
 * Seletor em pílula / chip consistente com a paleta escura.
 */
@Composable
fun OpcaoFiltroChip(
    selecionado: Boolean,
    rotulo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selecionado) OrangePrimary else SurfaceContainerHigh
    val corTexto = if (selecionado) OnOrangePrimary else CreamTertiary
    val borda = if (selecionado) OrangePrimary else BorderStrong

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = bg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, borda)
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = corTexto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/**
 * Estado vazio consistente (espelha Vazio de web/src/components/ui.tsx).
 */
@Composable
fun EstadoVazio(
    titulo: String,
    descricao: String? = null,
    acao: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStrong, RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            if (!descricao.isNullOrBlank()) {
                Text(
                    text = descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            if (acao != null) {
                Spacer(Modifier.height(8.dp))
                acao()
            }
        }
    }
}
