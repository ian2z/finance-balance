package br.com.financebalance.dto;

import br.com.financebalance.entity.TipoInvestimento;
import br.com.financebalance.entity.TipoMovimento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class InvestimentoDtos {
    private InvestimentoDtos() { }

    /** valorInicial gera um aporte inicial na criacao e e ignorado na atualizacao. */
    public record InvestimentoRequest(
            @NotBlank @Size(max = 100) String nome,
            @NotNull TipoInvestimento tipo,
            @NotNull LocalDate dataAplicacao,
            @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal valorInicial,
            @PositiveOrZero @Digits(integer = 13, fraction = 2) BigDecimal valorAtual) { }

    public record InvestimentoResponse(Long id, String nome, TipoInvestimento tipo, BigDecimal valorAplicado,
                                       BigDecimal valorAtual, BigDecimal rendimento,
                                       BigDecimal rentabilidadePercentual, LocalDate dataAplicacao) { }

    public record MovimentoRequest(
            @NotNull TipoMovimento tipo,
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @Digits(integer = 13, fraction = 2) BigDecimal valor,
            @NotNull LocalDate data) { }

    public record MovimentoResponse(Long id, TipoMovimento tipo, BigDecimal valor, LocalDate data) { }

    public record FatiaCarteira(TipoInvestimento tipo, BigDecimal valorAtual, BigDecimal percentual) { }

    public record ResumoCarteira(BigDecimal totalAplicado, BigDecimal totalAtual, BigDecimal rendimento,
                                 BigDecimal rentabilidadePercentual, List<FatiaCarteira> distribuicao) { }
}
