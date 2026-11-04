package br.com.financebalance.service;

import br.com.financebalance.dto.InvestimentoDtos.*;
import br.com.financebalance.entity.Investimento;
import br.com.financebalance.entity.MovimentoInvestimento;
import br.com.financebalance.entity.TipoInvestimento;
import br.com.financebalance.entity.TipoMovimento;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.InvestimentoRepository;
import br.com.financebalance.repository.MovimentoInvestimentoRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro manual de investimentos. Aportes aumentam o valor aplicado; resgates reduzem o
 * valor aplicado proporcionalmente; rendimentos alteram somente o valor atual.
 */
@Service
public class InvestimentoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final InvestimentoRepository investimentos;
    private final MovimentoInvestimentoRepository movimentos;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public InvestimentoService(InvestimentoRepository investimentos, MovimentoInvestimentoRepository movimentos,
                               UsuarioRepository usuarios, UsuarioAtual atual) {
        this.investimentos = investimentos;
        this.movimentos = movimentos;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public List<InvestimentoResponse> listar() {
        return investimentos.findByUsuarioIdOrderByNomeAsc(atual.id()).stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public InvestimentoResponse buscar(Long id) {
        return resposta(obter(id));
    }

    @Transactional
    public InvestimentoResponse criar(InvestimentoRequest req) {
        Investimento i = new Investimento();
        i.setUsuario(usuarios.getReferenceById(atual.id()));
        i.setNome(req.nome().trim());
        i.setTipo(req.tipo());
        i.setDataAplicacao(req.dataAplicacao());
        investimentos.save(i);
        BigDecimal inicial = req.valorInicial();
        if (inicial != null && inicial.signum() > 0) {
            i.setValorAplicado(inicial);
            i.setValorAtual(req.valorAtual() != null ? req.valorAtual() : inicial);
            registrar(i, TipoMovimento.APORTE, inicial, req.dataAplicacao());
        }
        return resposta(i);
    }

    /** Atualiza dados cadastrais e o valor atual informado manualmente. */
    @Transactional
    public InvestimentoResponse atualizar(Long id, InvestimentoRequest req) {
        Investimento i = obter(id);
        i.setNome(req.nome().trim());
        i.setTipo(req.tipo());
        i.setDataAplicacao(req.dataAplicacao());
        if (req.valorAtual() != null) {
            i.setValorAtual(req.valorAtual());
        }
        return resposta(i);
    }

    @Transactional
    public void excluir(Long id) {
        investimentos.delete(obter(id));
    }

    @Transactional(readOnly = true)
    public List<MovimentoResponse> listarMovimentos(Long id) {
        obter(id);
        return movimentos.findByInvestimentoIdOrderByDataDescIdDesc(id).stream()
                .map(m -> new MovimentoResponse(m.getId(), m.getTipo(), m.getValor(), m.getData())).toList();
    }

    @Transactional
    public InvestimentoResponse movimentar(Long id, MovimentoRequest req) {
        Investimento i = obter(id);
        BigDecimal v = req.valor();
        switch (req.tipo()) {
            case APORTE -> {
                i.setValorAplicado(i.getValorAplicado().add(v));
                i.setValorAtual(i.getValorAtual().add(v));
            }
            case RESGATE -> {
                if (v.compareTo(i.getValorAtual()) > 0) {
                    throw new RegraNegocioException("Resgate maior que o valor atual do investimento",
                            Map.of("valor", "máximo " + i.getValorAtual().toPlainString()));
                }
                BigDecimal custo = i.getValorAtual().signum() == 0 ? BigDecimal.ZERO
                        : i.getValorAplicado().multiply(v).divide(i.getValorAtual(), 2, RoundingMode.HALF_UP);
                i.setValorAplicado(i.getValorAplicado().subtract(custo).max(BigDecimal.ZERO));
                i.setValorAtual(i.getValorAtual().subtract(v));
            }
            case RENDIMENTO -> i.setValorAtual(i.getValorAtual().add(v));
        }
        registrar(i, req.tipo(), v, req.data());
        return resposta(i);
    }

    @Transactional(readOnly = true)
    public ResumoCarteira resumo() {
        List<Investimento> lista = investimentos.findByUsuarioIdOrderByNomeAsc(atual.id());
        BigDecimal aplicado = lista.stream().map(Investimento::getValorAplicado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal atualTotal = lista.stream().map(Investimento::getValorAtual).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<TipoInvestimento, BigDecimal> porTipo = new EnumMap<>(TipoInvestimento.class);
        lista.forEach(i -> porTipo.merge(i.getTipo(), i.getValorAtual(), BigDecimal::add));
        List<FatiaCarteira> fatias = porTipo.entrySet().stream()
                .map(e -> new FatiaCarteira(e.getKey(), e.getValue(), atualTotal.signum() == 0 ? BigDecimal.ZERO
                        : e.getValue().multiply(CEM).divide(atualTotal, 2, RoundingMode.HALF_UP)))
                .sorted((a, b) -> b.valorAtual().compareTo(a.valorAtual())).toList();
        return new ResumoCarteira(aplicado, atualTotal, atualTotal.subtract(aplicado), rentabilidade(aplicado, atualTotal),
                fatias);
    }

    private void registrar(Investimento i, TipoMovimento tipo, BigDecimal valor, java.time.LocalDate data) {
        MovimentoInvestimento m = new MovimentoInvestimento();
        m.setInvestimento(i);
        m.setTipo(tipo);
        m.setValor(valor);
        m.setData(data);
        movimentos.save(m);
    }

    private Investimento obter(Long id) {
        return investimentos.findByIdAndUsuarioId(id, atual.id())
                .orElseThrow(() -> new NaoEncontradoException("Investimento não encontrado"));
    }

    private static BigDecimal rentabilidade(BigDecimal aplicado, BigDecimal atual) {
        return aplicado.signum() == 0 ? BigDecimal.ZERO
                : atual.subtract(aplicado).multiply(CEM).divide(aplicado, 2, RoundingMode.HALF_UP);
    }

    private InvestimentoResponse resposta(Investimento i) {
        return new InvestimentoResponse(i.getId(), i.getNome(), i.getTipo(), i.getValorAplicado(), i.getValorAtual(),
                i.getValorAtual().subtract(i.getValorAplicado()), rentabilidade(i.getValorAplicado(), i.getValorAtual()),
                i.getDataAplicacao());
    }
}
