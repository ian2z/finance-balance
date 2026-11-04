package br.com.financebalance.service;

import br.com.financebalance.dto.OrcamentoDtos.OrcamentoRequest;
import br.com.financebalance.dto.OrcamentoDtos.OrcamentoResponse;
import br.com.financebalance.dto.RefDto;
import br.com.financebalance.entity.Categoria;
import br.com.financebalance.entity.Orcamento;
import br.com.financebalance.entity.TipoCategoria;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.exception.ConflitoException;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.CategoriaRepository;
import br.com.financebalance.repository.OrcamentoRepository;
import br.com.financebalance.repository.TransacaoRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O consumo do orcamento soma as despesas do mes na categoria (e subcategorias),
 * pagas ou pendentes: pendentes ja representam dinheiro comprometido.
 */
@Service
public class OrcamentoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);
    private static final BigDecimal LIMIAR_ALERTA = BigDecimal.valueOf(80);

    private final OrcamentoRepository orcamentos;
    private final CategoriaRepository categorias;
    private final TransacaoRepository transacoes;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public OrcamentoService(OrcamentoRepository orcamentos, CategoriaRepository categorias,
                            TransacaoRepository transacoes, UsuarioRepository usuarios, UsuarioAtual atual) {
        this.orcamentos = orcamentos;
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listar(LocalDate mes) {
        LocalDate primeiro = primeiroDia(mes != null ? mes : LocalDate.now());
        return orcamentos.findByUsuarioIdAndMesOrderByCategoriaNomeAsc(atual.id(), primeiro).stream()
                .map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscar(Long id) {
        return resposta(obter(id));
    }

    @Transactional
    public OrcamentoResponse criar(OrcamentoRequest req) {
        Long u = atual.id();
        LocalDate mes = primeiroDia(req.mes());
        Categoria c = categoriaDespesa(req.categoriaId(), u);
        if (orcamentos.existsByUsuarioIdAndCategoriaIdAndMes(u, c.getId(), mes)) {
            throw new ConflitoException("Já existe orçamento para essa categoria neste mês");
        }
        Orcamento o = new Orcamento();
        o.setUsuario(usuarios.getReferenceById(u));
        o.setCategoria(c);
        o.setMes(mes);
        o.setValorLimite(req.valorLimite());
        orcamentos.save(o);
        return resposta(o);
    }

    @Transactional
    public OrcamentoResponse atualizar(Long id, OrcamentoRequest req) {
        Long u = atual.id();
        Orcamento o = obter(id);
        LocalDate mes = primeiroDia(req.mes());
        Categoria c = categoriaDespesa(req.categoriaId(), u);
        if (orcamentos.existsByUsuarioIdAndCategoriaIdAndMesAndIdNot(u, c.getId(), mes, id)) {
            throw new ConflitoException("Já existe orçamento para essa categoria neste mês");
        }
        o.setCategoria(c);
        o.setMes(mes);
        o.setValorLimite(req.valorLimite());
        return resposta(o);
    }

    @Transactional
    public void excluir(Long id) {
        orcamentos.delete(obter(id));
    }

    /** Soma [limite, utilizado] de todos os orcamentos do mes, para o dashboard. */
    @Transactional(readOnly = true)
    public BigDecimal[] totaisDoMes(LocalDate mes) {
        BigDecimal limite = BigDecimal.ZERO;
        BigDecimal usado = BigDecimal.ZERO;
        for (Orcamento o : orcamentos.findByUsuarioIdAndMesOrderByCategoriaNomeAsc(atual.id(), primeiroDia(mes))) {
            limite = limite.add(o.getValorLimite());
            usado = usado.add(utilizado(o));
        }
        return new BigDecimal[] {limite, usado};
    }

    private Orcamento obter(Long id) {
        return orcamentos.findByIdAndUsuarioId(id, atual.id())
                .orElseThrow(() -> new NaoEncontradoException("Orçamento não encontrado"));
    }

    private Categoria categoriaDespesa(Long id, Long u) {
        Categoria c = categorias.findVisivel(id, u).orElseThrow(() -> new RegraNegocioException(
                "Categoria inválida", Map.of("categoriaId", "categoria não encontrada")));
        if (c.getTipo() != TipoCategoria.DESPESA) {
            throw new RegraNegocioException("Orçamentos só se aplicam a categorias de despesa",
                    Map.of("categoriaId", "deve ser uma categoria de despesa"));
        }
        return c;
    }

    private BigDecimal utilizado(Orcamento o) {
        YearMonth ym = YearMonth.from(o.getMes());
        return transacoes.somaDespesasDaCategoria(atual.id(), o.getCategoria().getId(), TipoTransacao.DESPESA,
                ym.atDay(1), ym.atEndOfMonth());
    }

    private OrcamentoResponse resposta(Orcamento o) {
        BigDecimal usado = utilizado(o);
        BigDecimal pct = usado.multiply(CEM).divide(o.getValorLimite(), 2, RoundingMode.HALF_UP);
        String situacao = pct.compareTo(CEM) >= 0 ? "ESTOURADO" : pct.compareTo(LIMIAR_ALERTA) >= 0
                ? "PROXIMO_DO_LIMITE" : "DENTRO_DO_LIMITE";
        return new OrcamentoResponse(o.getId(), new RefDto(o.getCategoria().getId(), o.getCategoria().getNome()),
                o.getMes(), o.getValorLimite(), usado, o.getValorLimite().subtract(usado), pct, situacao);
    }

    private static LocalDate primeiroDia(LocalDate d) {
        return d.withDayOfMonth(1);
    }
}
