package br.com.financebalance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class DistribuicaoDtos {
    private DistribuicaoDtos() { }

    public record ItemRequest(
            @NotNull Long categoriaId,
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @DecimalMax(value = "100", message = "deve ser no máximo 100")
            @Digits(integer = 3, fraction = 2) BigDecimal percentual) { }

    public record DistribuicaoRequest(
            @NotNull @DecimalMin(value = "0.01", message = "deve ser maior que zero")
            @Digits(integer = 13, fraction = 2) BigDecimal rendaBase,
            @NotNull @Valid List<ItemRequest> itens) { }

    public record ItemResponse(RefDto categoria, BigDecimal percentual, BigDecimal valorPlanejado,
                               BigDecimal valorRealizado) { }

    public record DistribuicaoResponse(BigDecimal rendaBase, BigDecimal totalPercentual,
                                       BigDecimal percentualNaoAlocado, List<ItemResponse> itens) { }
}
