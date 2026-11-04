package br.com.financebalance.dto;

import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.entity.Transacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class TransacaoDtos {
    private TransacaoDtos() { }

    /** Receita ou despesa. Transferencias usam {@link TransferenciaRequest}. */
    public record TransacaoRequest(
            @NotBlank @Size(max = 200) String descricao,
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @Digits(integer = 13, fraction = 2) BigDecimal valor,
            @NotNull TipoTransacao tipo,
            @NotNull LocalDate data,
            LocalDate dataVencimento,
            SituacaoTransacao situacao,
            @NotNull Long categoriaId,
            @NotNull Long contaId,
            Long formaPagamentoId,
            @Size(max = 500) String observacao,
            Set<Long> etiquetaIds) { }

    public record TransferenciaRequest(
            @NotBlank @Size(max = 200) String descricao,
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @Digits(integer = 13, fraction = 2) BigDecimal valor,
            @NotNull LocalDate data,
            @NotNull Long contaOrigemId,
            @NotNull Long contaDestinoId,
            @Size(max = 500) String observacao) { }

    public record TransacaoResponse(
            Long id, String descricao, BigDecimal valor, TipoTransacao tipo, SituacaoTransacao situacao,
            LocalDate data, LocalDate dataVencimento, String observacao, RefDto categoria, RefDto conta,
            RefDto contaDestino, RefDto formaPagamento, List<RefDto> etiquetas) {

        public static TransacaoResponse de(Transacao t) {
            return new TransacaoResponse(t.getId(), t.getDescricao(), t.getValor(), t.getTipo(), t.getSituacao(),
                    t.getData(), t.getDataVencimento(), t.getObservacao(),
                    t.getCategoria() == null ? null : new RefDto(t.getCategoria().getId(), t.getCategoria().getNome()),
                    new RefDto(t.getConta().getId(), t.getConta().getNome()),
                    t.getContaDestino() == null ? null
                            : new RefDto(t.getContaDestino().getId(), t.getContaDestino().getNome()),
                    t.getFormaPagamento() == null ? null
                            : new RefDto(t.getFormaPagamento().getId(), t.getFormaPagamento().getNome()),
                    t.getEtiquetas().stream().map(e -> new RefDto(e.getId(), e.getNome()))
                            .sorted(Comparator.comparing(RefDto::nome)).toList());
        }
    }
}
