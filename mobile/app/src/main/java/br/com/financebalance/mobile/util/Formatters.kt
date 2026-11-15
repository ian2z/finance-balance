package br.com.financebalance.mobile.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localePtBr = Locale("pt", "BR")
private val formatoMoeda = NumberFormat.getCurrencyInstance(localePtBr)
private val formatoPercentual = NumberFormat.getNumberInstance(localePtBr).apply {
    minimumFractionDigits = 1
    maximumFractionDigits = 1
}
private val formatoDataBr = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val formatoMesLongo = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", localePtBr)

fun formatarMoeda(valor: BigDecimal?): String {
    return formatoMoeda.format(valor ?: BigDecimal.ZERO)
}

fun formatarMoeda(valor: Double): String {
    return formatoMoeda.format(valor)
}

fun formatarPercentual(valor: BigDecimal?): String {
    return "${formatoPercentual.format(valor ?: BigDecimal.ZERO)}%"
}

fun formatarPercentual(valor: Float): String {
    return "${formatoPercentual.format(valor.toDouble())}%"
}

/**
 * Converte data ISO ('AAAA-MM-DD') para formato brasileiro ('DD/MM/AAAA').
 */
fun formatarDataBr(dataIso: String?): String {
    if (dataIso.isNullOrBlank()) return "—"
    val limpo = dataIso.trim().take(10)
    // Se já estiver em DD/MM/AAAA, retorna como está
    if (Regex("""^\d{2}/\d{2}/\d{4}$""").matches(limpo)) return limpo
    return try {
        LocalDate.parse(limpo).format(formatoDataBr)
    } catch (_: Exception) {
        dataIso
    }
}

/**
 * Converte texto digitado em 'DD/MM/AAAA' (ou 'AAAA-MM-DD') para ISO 'AAAA-MM-DD'.
 * Retorna null se a data for inválida.
 */
fun converterDataBrParaIso(texto: String): String? {
    val limpo = texto.trim()
    if (limpo.isEmpty()) return null
    return try {
        if (Regex("""^\d{2}/\d{2}/\d{4}$""").matches(limpo)) {
            LocalDate.parse(limpo, formatoDataBr).toString()
        } else if (Regex("""^\d{4}-\d{2}-\d{2}$""").matches(limpo)) {
            LocalDate.parse(limpo).toString()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

/**
 * Formata automaticamente dígitos de data enquanto o usuário digita (ex: "14112026" -> "14/11/2026").
 */
fun aplicarMascaraDataBr(input: String): String {
    // Se já tem barras, permite edição natural mantendo apenas dígitos e barras até 10 chars
    if (input.contains("/")) {
        return input.filter { it.isDigit() || it == '/' }.take(10)
    }
    val digitos = input.filter { it.isDigit() }.take(8)
    return buildString {
        for (i in digitos.indices) {
            append(digitos[i])
            if ((i == 1 || i == 3) && i != digitos.lastIndex) {
                append('/')
            }
        }
    }
}

fun isoParaEpochMillisUtc(dataIso: String?): Long? {
    val iso = dataIso?.let { converterDataBrParaIso(it) } ?: return null
    return try {
        LocalDate.parse(iso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    } catch (_: Exception) {
        null
    }
}

fun epochMillisUtcParaDataBr(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    return date.format(formatoDataBr)
}

fun hojeIso(): String = LocalDate.now().toString()

fun hojeBr(): String = LocalDate.now().format(formatoDataBr)

fun dataFuturaBr(mesesAFrente: Long): String =
    LocalDate.now().plusMonths(mesesAFrente).format(formatoDataBr)

fun mesAtualIso(): String = YearMonth.now().toString() // AAAA-MM

fun deslocarMes(anoMes: String, delta: Long): String {
    return try {
        YearMonth.parse(anoMes).plusMonths(delta).toString()
    } catch (_: Exception) {
        mesAtualIso()
    }
}

fun limitesDoMes(anoMes: String): Pair<String, String> {
    val ym = try {
        YearMonth.parse(anoMes)
    } catch (_: Exception) {
        YearMonth.now()
    }
    return ym.atDay(1).toString() to ym.atEndOfMonth().toString()
}

fun nomeDoMes(anoMes: String): String {
    val ym = try {
        YearMonth.parse(anoMes)
    } catch (_: Exception) {
        YearMonth.now()
    }
    val texto = ym.atDay(1).format(formatoMesLongo)
    return texto.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localePtBr) else it.toString() }
}

/**
 * Converte valor numérico para string com vírgula brasileira para edição (ex: 1250.5 -> "1250,50").
 */
fun valorParaCampoBr(valor: BigDecimal?): String {
    if (valor == null) return ""
    return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')
}

fun valorParaCampoBr(valor: Double?): String {
    if (valor == null || valor == 0.0) return ""
    return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',')
}

/**
 * Lê um valor monetário digitado em formato brasileiro ("1.234,56" ou "1234,56" ou "1234.56").
 */
fun lerValorBigDecimal(texto: String): BigDecimal? {
    val t = texto.trim()
    if (t.isEmpty()) return null
    val normalizado = if (t.contains(',')) {
        t.replace(".", "").replace(',', '.')
    } else {
        t
    }
    return normalizado.toBigDecimalOrNull()
}

fun lerValorDouble(texto: String): Double? = lerValorBigDecimal(texto)?.toDouble()

fun rotuloTipoConta(tipo: String): String = when (tipo) {
    "CORRENTE" -> "Conta corrente"
    "POUPANCA" -> "Poupança"
    "CARTEIRA" -> "Carteira"
    "DINHEIRO" -> "Dinheiro em espécie"
    "CARTEIRA_DIGITAL" -> "Carteira digital"
    else -> tipo
}

fun rotuloPrioridade(prioridade: String): String = when (prioridade) {
    "ALTA" -> "Prioridade alta"
    "MEDIA" -> "Prioridade média"
    "BAIXA" -> "Prioridade baixa"
    else -> prioridade
}
