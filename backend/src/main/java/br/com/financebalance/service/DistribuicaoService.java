package br.com.financebalance.service;

import br.com.financebalance.dto.DistribuicaoDtos.DistribuicaoRequest;
import br.com.financebalance.dto.DistribuicaoDtos.DistribuicaoResponse;
import br.com.financebalance.dto.DistribuicaoDtos.ItemRequest;
import br.com.financebalance.dto.DistribuicaoDtos.ItemResponse;
import br.com.financebalance.dto.RefDto;
import br.com.financebalance.entity.Categoria;
import br.com.financebalance.entity.RegraDistribuicao;
import br.com.financebalance.entity.TipoCategoria;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.entity.Usuario;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.CategoriaRepository;
import br.com.financebalance.repository.RegraDistribuicaoRepository;
import br.com.financebalance.repository.TransacaoRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Distribuicao percentual da renda. E planejamento: o valor planejado nao
 * movimenta dinheiro; o realizado vem das despesas do mes.
 */
@Service
public class DistribuicaoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final RegraDistribuicaoRepository regras;
    private final CategoriaRepository categorias;
    private final TransacaoRepository transacoes;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public DistribuicaoService(RegraDistribuicaoRepository regras, CategoriaRepository categorias,
                               TransacaoRepository transacoes, UsuarioRepository usuarios, UsuarioAtual atual) {
        this.regras = regras;
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public DistribuicaoResponse consultar(LocalDate mes) {
        Long u = atual.id();
        Usuario usuario = usuarios.findById(u).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
        return montar(usuario, mes != null ? mes : LocalDate.now());
    }

    /** Substitui integralmente as regras de distribuicao do usuario. */
    @Transactional
    public DistribuicaoResponse definir(DistribuicaoRequest req) {
        Long u = atual.id();

        Set<Long> vistos = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ItemRequest i : req.itens()) {
            if (!vistos.add(i.categoriaId())) {
                throw new RegraNegocioException("Categoria repetida na distribuição",
                        Map.of("itens", "categoria " + i.categoriaId() + " repetida"));
            }
            total = total.add(i.percentual());
        }
        if (total.compareTo(CEM) > 0) {
            throw new RegraNegocioException("A soma dos percentuais não pode ultrapassar 100%",
                    Map.of("itens", "soma atual: " + total.stripTrailingZeros().toPlainString() + "%"));
        }

        regras.apagarDoUsuario(u); // limpa o contexto de persistencia: carregar o usuario depois
        Usuario usuario = usuarios.findById(u).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
        for (ItemRequest i : req.itens()) {
            Categoria c = categorias.findVisivel(i.categoriaId(), u).orElseThrow(() -> new RegraNegocioException(
                    "Categoria inválida", Map.of("itens", "categoria " + i.categoriaId() + " não encontrada")));
            if (c.getTipo() != TipoCategoria.DESPESA) {
                throw new RegraNegocioException("A distribuição só usa categorias de despesa",
                        Map.of("itens", "categoria " + c.getNome() + " não é de despesa"));
            }
            RegraDistribuicao r = new RegraDistribuicao();
            r.setUsuario(usuario);
            r.setCategoria(c);
            r.setPercentual(i.percentual());
            regras.save(r);
        }
        usuario.setRendaMensal(req.rendaBase());
        regras.flush();
        return montar(usuario, LocalDate.now());
    }

    private DistribuicaoResponse montar(Usuario usuario, LocalDate mes) {
        BigDecimal renda = usuario.getRendaMensal() != null ? usuario.getRendaMensal() : BigDecimal.ZERO;
        YearMonth ym = YearMonth.from(mes);
        BigDecimal total = BigDecimal.ZERO;
        List<ItemResponse> itens = new java.util.ArrayList<>();
        for (RegraDistribuicao r : regras.findByUsuarioIdOrderByPercentualDesc(usuario.getId())) {
            total = total.add(r.getPercentual());
            BigDecimal planejado = renda.multiply(r.getPercentual()).divide(CEM, 2, RoundingMode.HALF_UP);
            BigDecimal realizado = transacoes.somaDespesasDaCategoria(usuario.getId(), r.getCategoria().getId(),
                    TipoTransacao.DESPESA, ym.atDay(1), ym.atEndOfMonth());
            itens.add(new ItemResponse(new RefDto(r.getCategoria().getId(), r.getCategoria().getNome()),
                    r.getPercentual(), planejado, realizado));
        }
        return new DistribuicaoResponse(renda, total, CEM.subtract(total), itens);
    }
}
