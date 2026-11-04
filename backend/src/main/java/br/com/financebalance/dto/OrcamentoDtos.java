package br.com.financebalance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class OrcamentoDtos {
    private OrcamentoDtos() { }

    public record OrcamentoRequest(
            @NotNull Long categoriaId,
            @NotNull LocalDate mes,
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @Digits(integer = 13, fraction = 2) BigDecimal valorLimite) { }

    public record OrcamentoResponse(Long id, RefDto categoria, LocalDate mes, BigDecimal valorLimite,
                                    BigDecimal valorUtilizado, BigDecimal valorRestante,
                                    BigDecimal percentualConsumido, String situacao) { }
}
