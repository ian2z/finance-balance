package br.com.financebalance.mobile.data.firestore

/**
 * Modelo de Meta Financeira (Módulo 16 do escopo) persistido no Cloud Firestore.
 * Vinculado ao usuário autenticado no backend Spring Boot via [usuarioId] e [usuarioEmail].
 */
data class MetaFinanceira(
    val id: String = "",
    val usuarioId: Long = 0L,
    val usuarioEmail: String = "",
    val titulo: String = "",
    val categoria: String = "Reserva de Emergência",
    val valorAlvo: Double = 0.0,
    val valorAtual: Double = 0.0,
    val prazo: String = "", // formato ISO AAAA-MM-DD
    val prioridade: String = "MEDIA", // "ALTA" | "MEDIA" | "BAIXA"
    val observacao: String = "",
    val concluida: Boolean = false,
    val criadoEm: Long = System.currentTimeMillis()
) {
    val percentualConcluido: Float
        get() = if (valorAlvo > 0.0) {
            ((valorAtual / valorAlvo) * 100.0).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }

    val valorRestante: Double
        get() = (valorAlvo - valorAtual).coerceAtLeast(0.0)
}
