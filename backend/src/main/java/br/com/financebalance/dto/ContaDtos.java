package br.com.financebalance.dto;

import br.com.financebalance.entity.TipoConta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class ContaDtos {
    private ContaDtos() { }

    public record ContaRequest(
            @NotBlank @Size(max = 80) String nome,
            @NotNull TipoConta tipo,
            @NotNull @Digits(integer = 13, fraction = 2) BigDecimal saldoInicial,
            Boolean ativa) { }

    public record ContaResponse(Long id, String nome, TipoConta tipo, BigDecimal saldoInicial,
                                BigDecimal saldoAtual, boolean ativa) { }
}
