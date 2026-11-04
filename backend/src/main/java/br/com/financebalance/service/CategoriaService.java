package br.com.financebalance.service;

import br.com.financebalance.dto.CategoriaDtos.CategoriaRequest;
import br.com.financebalance.dto.CategoriaDtos.CategoriaResponse;
import br.com.financebalance.entity.Categoria;
import br.com.financebalance.entity.TipoCategoria;
import br.com.financebalance.exception.AcessoNegadoException;
import br.com.financebalance.exception.ConflitoException;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.CategoriaRepository;
import br.com.financebalance.repository.TransacaoRepository;
import br.com.financebalance.repository.UsuarioRepository;
import br.com.financebalance.security.UsuarioAtual;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final TransacaoRepository transacoes;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public CategoriaService(CategoriaRepository categorias, TransacaoRepository transacoes,
                            UsuarioRepository usuarios, UsuarioAtual atual) {
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(TipoCategoria tipo) {
        Long u = atual.id();
        Set<Long> comMovimento = new HashSet<>();
        transacoes.contagemPorCategoria(u).forEach(l -> comMovimento.add((Long) l[0]));
        return categorias.findVisiveis(u).stream()
                .filter(c -> tipo == null || c.getTipo() == tipo)
                .map(c -> resposta(c, !comMovimento.contains(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        Long u = atual.id();
        Categoria c = categorias.findVisivel(id, u)
                .orElseThrow(() -> new NaoEncontradoException("Categoria não encontrada"));
        return resposta(c, !transacoes.existsByCategoriaId(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest req) {
        Long u = atual.id();
        String nome = req.nome().trim();
        Categoria pai = resolverPai(req, u);
        if (duplicada(u, nome, pai == null ? null : pai.getId(), -1L)) {
            throw new ConflitoException("Já existe uma categoria com esse nome neste nível");
        }
        Categoria c = new Categoria();
        c.setUsuario(usuarios.getReferenceById(u));
        c.setNome(nome);
        c.setTipo(pai != null ? pai.getTipo() : req.tipo());
        c.setCategoriaPai(pai);
        categorias.save(c);
        return resposta(c, true);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest req) {
        Long u = atual.id();
        Categoria c = obterEditavel(id, u);
        String nome = req.nome().trim();
        Categoria pai = resolverPai(req, u);
        Long paiId = pai == null ? null : pai.getId();
        boolean mudouEstrutura = c.getTipo() != (pai != null ? pai.getTipo() : req.tipo())
                || !java.util.Objects.equals(paiId, c.getCategoriaPai() == null ? null : c.getCategoriaPai().getId());
        if (id.equals(paiId)) {
            throw new RegraNegocioException("Uma categoria não pode ser subcategoria de si mesma");
        }
        if (pai != null && categorias.existsByCategoriaPaiId(id)) {
            throw new RegraNegocioException("Categoria com subcategorias não pode virar subcategoria");
        }
        if (mudouEstrutura && (transacoes.existsByCategoriaId(id) || categorias.existsByCategoriaPaiId(id))) {
            throw new ConflitoException("Não é possível alterar tipo ou categoria pai de categoria em uso");
        }
        if (duplicada(u, nome, paiId, id)) {
            throw new ConflitoException("Já existe uma categoria com esse nome neste nível");
        }
        c.setNome(nome);
        c.setTipo(pai != null ? pai.getTipo() : req.tipo());
        c.setCategoriaPai(pai);
        return resposta(c, !transacoes.existsByCategoriaId(id));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria c = obterEditavel(id, atual.id());
        if (categorias.existsByCategoriaPaiId(id)) {
            throw new ConflitoException("A categoria possui subcategorias");
        }
        if (transacoes.existsByCategoriaId(id)) {
            throw new ConflitoException("A categoria possui movimentações");
        }
        categorias.delete(c);
    }

    private boolean duplicada(Long usuarioId, String nome, Long paiId, Long ignorarId) {
        return paiId == null
                ? categorias.existsByUsuarioIdAndNomeIgnoreCaseAndCategoriaPaiIsNullAndIdNot(usuarioId, nome, ignorarId)
                : categorias.existsByUsuarioIdAndNomeIgnoreCaseAndCategoriaPaiIdAndIdNot(usuarioId, nome, paiId, ignorarId);
    }

    private Categoria obterEditavel(Long id, Long usuarioId) {
        Categoria c = categorias.findVisivel(id, usuarioId)
                .orElseThrow(() -> new NaoEncontradoException("Categoria não encontrada"));
        if (c.isDoSistema()) {
            throw new AcessoNegadoException("Categorias padrão não podem ser alteradas");
        }
        return c;
    }

    private Categoria resolverPai(CategoriaRequest req, Long usuarioId) {
        if (req.categoriaPaiId() == null) {
            return null;
        }
        Categoria pai = categorias.findVisivel(req.categoriaPaiId(), usuarioId)
                .orElseThrow(() -> new RegraNegocioException("Categoria pai inválida",
                        Map.of("categoriaPaiId", "categoria não encontrada")));
        if (pai.getCategoriaPai() != null) {
            throw new RegraNegocioException("Subcategorias só podem ter um nível",
                    Map.of("categoriaPaiId", "a categoria pai já é uma subcategoria"));
        }
        if (pai.getTipo() != req.tipo()) {
            throw new RegraNegocioException("O tipo da subcategoria deve ser igual ao da categoria pai",
                    Map.of("tipo", "deve ser " + pai.getTipo()));
        }
        return pai;
    }

    private CategoriaResponse resposta(Categoria c, boolean semMovimentacoes) {
        return new CategoriaResponse(c.getId(), c.getNome(), c.getTipo(),
                c.getCategoriaPai() == null ? null : c.getCategoriaPai().getId(), c.isDoSistema(), semMovimentacoes);
    }
}
