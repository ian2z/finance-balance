package br.com.financebalance.specification;

import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import java.time.LocalDate;

/** Filtros opcionais de busca de transacoes. */
public record TransacaoFiltro(
        String texto,
        Long categoriaId,
        Long contaId,
        TipoTransacao tipo,
        SituacaoTransacao situacao,
        Long etiquetaId,
        LocalDate de,
        LocalDate ate) {
}
