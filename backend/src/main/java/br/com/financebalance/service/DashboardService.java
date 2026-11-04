package br.com.financebalance.service;

import br.com.financebalance.dto.DashboardDtos.*;
import br.com.financebalance.entity.Conta;
import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.ContaRepository;
import br.com.financebalance.repository.TransacaoRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Totais do periodo: considera apenas movimentacoes pagas (pendentes aparecem a parte)
 * e ignora transferencias entre contas.
 */
@Service
public class DashboardService {

    private static final int MESES_EVOLUCAO = 6;
    private static final int MAIORES_GASTOS = 5;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final TransacaoRepository transacoes;
    private final ContaRepository contas;
    private final ContaService contaService;
    private final OrcamentoService orcamentoService;
    private final UsuarioAtual atual;

    public DashboardService(TransacaoRepository transacoes, ContaRepository contas, ContaService contaService,
                            OrcamentoService orcamentoService, UsuarioAtual atual) {
        this.transacoes = transacoes;
        this.contas = contas;
        this.contaService = contaService;
        this.orcamentoService = orcamentoService;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public ResumoResponse resumo(LocalDate de, LocalDate ate) {
        YearMonth atualYm = YearMonth.now();
        LocalDate inicio = de != null ? de : atualYm.atDay(1);
        LocalDate fim = ate != null ? ate : atualYm.atEndOfMonth();
        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("Período inválido", Map.of("de", "deve ser anterior ou igual a 'ate'"));
        }
        Long u = atual.id();

        BigDecimal receitas = BigDecimal.ZERO, despesas = BigDecimal.ZERO;
        BigDecimal receitasPend = BigDecimal.ZERO, despesasPend = BigDecimal.ZERO;
        for (Object[] l : transacoes.totaisPorTipoESituacao(u, inicio, fim, TipoTransacao.TRANSFERENCIA)) {
            boolean paga = l[1] == SituacaoTransacao.PAGA;
            BigDecimal v = (BigDecimal) l[2];
            if (l[0] == TipoTransacao.RECEITA) {
                if (paga) receitas = v; else receitasPend = v;
            } else {
                if (paga) despesas = v; else despesasPend = v;
            }
        }

        List<Conta> ativas = contas.findByUsuarioIdOrderByNomeAsc(u).stream().filter(Conta::isAtiva).toList();
        BigDecimal saldoTotal = contaService.saldosAtuais(u, ativas).values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDespesasCat = despesas;
        List<GastoCategoria> porCategoria = transacoes
                .totaisPorCategoria(u, TipoTransacao.DESPESA, SituacaoTransacao.PAGA, inicio, fim).stream()
                .map(l -> new GastoCategoria((Long) l[0], (String) l[1], (BigDecimal) l[2],
                        totalDespesasCat.signum() == 0 ? BigDecimal.ZERO
                                : ((BigDecimal) l[2]).multiply(CEM).divide(totalDespesasCat, 2, RoundingMode.HALF_UP)))
                .toList();

        List<MaiorGasto> maiores = transacoes.maioresValores(u, TipoTransacao.DESPESA, SituacaoTransacao.PAGA,
                        inicio, fim, PageRequest.of(0, MAIORES_GASTOS)).stream()
                .map(t -> new MaiorGasto(t.getId(), t.getDescricao(), t.getValor(), t.getData(),
                        t.getCategoria() == null ? null : t.getCategoria().getNome()))
                .toList();

        BigDecimal[] orc = orcamentoService.totaisDoMes(fim);
        BigDecimal pctOrc = orc[0].signum() == 0 ? BigDecimal.ZERO
                : orc[1].multiply(CEM).divide(orc[0], 2, RoundingMode.HALF_UP);

        return new ResumoResponse(inicio, fim, saldoTotal, receitas, despesas, receitas.subtract(despesas),
                receitasPend, despesasPend, porCategoria, evolucao(u, YearMonth.from(fim)), maiores,
                new OrcamentoConsumido(orc[0], orc[1], pctOrc));
    }

    private List<PontoMensal> evolucao(Long u, YearMonth ultimo) {
        YearMonth primeiro = ultimo.minusMonths(MESES_EVOLUCAO - 1L);
        Map<YearMonth, BigDecimal[]> meses = new HashMap<>();
        for (Object[] l : transacoes.totaisMensais(u, primeiro.atDay(1), ultimo.atEndOfMonth(),
                SituacaoTransacao.PAGA, TipoTransacao.TRANSFERENCIA)) {
            YearMonth ym = YearMonth.of(((Number) l[0]).intValue(), ((Number) l[1]).intValue());
            BigDecimal[] par = meses.computeIfAbsent(ym, k -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
            par[l[2] == TipoTransacao.RECEITA ? 0 : 1] = (BigDecimal) l[3];
        }
        List<PontoMensal> pontos = new ArrayList<>();
        for (YearMonth ym = primeiro; !ym.isAfter(ultimo); ym = ym.plusMonths(1)) {
            BigDecimal[] par = meses.getOrDefault(ym, new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
            pontos.add(new PontoMensal(ym.toString(), par[0], par[1]));
        }
        return pontos;
    }
}
