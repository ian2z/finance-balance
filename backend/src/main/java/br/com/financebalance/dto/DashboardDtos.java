package br.com.financebalance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() { }

    public record GastoCategoria(Long categoriaId, String categoria, BigDecimal valor, BigDecimal percentual) { }

    public record PontoMensal(String mes, BigDecimal receitas, BigDecimal despesas) { }

    public record MaiorGasto(Long id, String descricao, BigDecimal valor, LocalDate data, String categoria) { }

    public record OrcamentoConsumido(BigDecimal totalLimite, BigDecimal totalUtilizado, BigDecimal percentual) { }

    public record ResumoResponse(
            LocalDate de, LocalDate ate,
            BigDecimal saldoTotal,
            BigDecimal receitas, BigDecimal despesas, BigDecimal resultado,
            BigDecimal receitasPendentes, BigDecimal despesasPendentes,
            List<GastoCategoria> despesasPorCategoria,
            List<PontoMensal> evolucao,
            List<MaiorGasto> maioresGastos,
            OrcamentoConsumido orcamento) { }
}
