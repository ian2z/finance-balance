package br.com.financebalance.mobile.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repositório responsável pelo CRUD em nuvem utilizando o Google Cloud Firestore.
 * Coleção: "metas_financeiras".
 */
class MetasFirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colecao = firestore.collection("metas_financeiras")

    /**
     * READ: Lista todas as metas financeiras do usuário autenticado ordenadas por criação.
     */
    suspend fun listarPorUsuario(usuarioId: Long): List<MetaFinanceira> {
        val snapshot = colecao
            .whereEqualTo("usuarioId", usuarioId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val valorAlvo = (doc.get("valorAlvo") as? Number)?.toDouble() ?: 0.0
            val valorAtual = (doc.get("valorAtual") as? Number)?.toDouble() ?: 0.0
            MetaFinanceira(
                id = doc.id,
                usuarioId = (doc.get("usuarioId") as? Number)?.toLong() ?: usuarioId,
                usuarioEmail = doc.getString("usuarioEmail") ?: "",
                titulo = doc.getString("titulo") ?: "",
                categoria = doc.getString("categoria") ?: "Geral",
                valorAlvo = valorAlvo,
                valorAtual = valorAtual,
                prazo = doc.getString("prazo") ?: "",
                prioridade = doc.getString("prioridade") ?: "MEDIA",
                observacao = doc.getString("observacao") ?: "",
                concluida = doc.getBoolean("concluida") ?: (valorAtual >= valorAlvo && valorAlvo > 0.0),
                criadoEm = (doc.get("criadoEm") as? Number)?.toLong() ?: 0L
            )
        }.sortedByDescending { it.criadoEm }
    }

    /**
     * CREATE: Insere um novo documento de meta financeira no Cloud Firestore.
     */
    suspend fun criar(meta: MetaFinanceira): MetaFinanceira {
        val docRef = colecao.document()
        val concluida = meta.valorAtual >= meta.valorAlvo && meta.valorAlvo > 0.0
        val dados = mapOf(
            "usuarioId" to meta.usuarioId,
            "usuarioEmail" to meta.usuarioEmail,
            "titulo" to meta.titulo.trim(),
            "categoria" to meta.categoria.trim(),
            "valorAlvo" to meta.valorAlvo,
            "valorAtual" to meta.valorAtual,
            "prazo" to meta.prazo.trim(),
            "prioridade" to meta.prioridade,
            "observacao" to meta.observacao.trim(),
            "concluida" to concluida,
            "criadoEm" to System.currentTimeMillis()
        )
        docRef.set(dados).await()
        return meta.copy(id = docRef.id, concluida = concluida)
    }

    /**
     * UPDATE: Atualiza os dados de uma meta existente no Cloud Firestore.
     */
    suspend fun atualizar(meta: MetaFinanceira): MetaFinanceira {
        require(meta.id.isNotBlank()) { "ID da meta é obrigatório para atualização." }
        val concluida = meta.valorAtual >= meta.valorAlvo && meta.valorAlvo > 0.0
        val dados = mapOf(
            "titulo" to meta.titulo.trim(),
            "categoria" to meta.categoria.trim(),
            "valorAlvo" to meta.valorAlvo,
            "valorAtual" to meta.valorAtual,
            "prazo" to meta.prazo.trim(),
            "prioridade" to meta.prioridade,
            "observacao" to meta.observacao.trim(),
            "concluida" to concluida
        )
        colecao.document(meta.id).update(dados).await()
        return meta.copy(concluida = concluida)
    }

    /**
     * UPDATE (Aporte rápido): Soma um valor ao saldo acumulado da meta no Cloud Firestore.
     */
    suspend fun registrarAporte(meta: MetaFinanceira, valorAporte: Double): MetaFinanceira {
        val novoValorAtual = (meta.valorAtual + valorAporte).coerceAtLeast(0.0)
        return atualizar(meta.copy(valorAtual = novoValorAtual))
    }

    /**
     * DELETE: Remove o documento da meta financeira do Cloud Firestore.
     */
    suspend fun excluir(metaId: String) {
        colecao.document(metaId).delete().await()
    }
}
