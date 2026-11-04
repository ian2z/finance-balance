package br.com.financebalance.service;

import br.com.financebalance.dto.PaginaResponse;
import br.com.financebalance.dto.TransacaoDtos.TransacaoRequest;
import br.com.financebalance.dto.TransacaoDtos.TransacaoResponse;
import br.com.financebalance.dto.TransacaoDtos.TransferenciaRequest;
import br.com.financebalance.entity.*;
import br.com.financebalance.exception.NaoEncontradoException;
import br.com.financebalance.exception.RegraNegocioException;
import br.com.financebalance.repository.*;
import br.com.financebalance.security.UsuarioAtual;
import br.com.financebalance.specification.TransacaoFiltro;
import br.com.financebalance.specification.TransacaoSpecs;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransacaoService {

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("data", "valor", "descricao", "criadoEm");
    /** Limite de linhas em uma exportacao, para proteger a memoria do servidor. */
    static final int LIMITE_EXPORTACAO = 50_000;

    private final TransacaoRepository transacoes;
    private final ContaRepository contas;
    private final CategoriaRepository categorias;
    private final FormaPagamentoRepository formas;
    private final EtiquetaRepository etiquetas;
    private final UsuarioRepository usuarios;
    private final UsuarioAtual atual;

    public TransacaoService(TransacaoRepository transacoes, ContaRepository contas, CategoriaRepository categorias,
                            FormaPagamentoRepository formas, EtiquetaRepository etiquetas,
                            UsuarioRepository usuarios, UsuarioAtual atual) {
        this.transacoes = transacoes;
        this.contas = contas;
        this.categorias = categorias;
        this.formas = formas;
        this.etiquetas = etiquetas;
        this.usuarios = usuarios;
        this.atual = atual;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<TransacaoResponse> listar(TransacaoFiltro filtro, Pageable pageable) {
        validarPeriodo(filtro);
        Pageable p = pageable.getSort().isSorted() ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), ordenacaoPadrao());
        p.getSort().forEach(o -> {
            if (!CAMPOS_ORDENAVEIS.contains(o.getProperty())) {
                throw new RegraNegocioException("Campo de ordenação inválido: " + o.getProperty());
            }
        });
        // desempate estavel para paginacao
        Sort sort = p.getSort().and(Sort.by(Sort.Direction.DESC, "id"));
        var page = transacoes.findAll(TransacaoSpecs.filtrar(atual.id(), filtro),
                PageRequest.of(p.getPageNumber(), p.getPageSize(), sort));
        return PaginaResponse.de(page, TransacaoResponse::de);
    }

    @Transactional(readOnly = true)
    public List<Transacao> listarParaExportacao(TransacaoFiltro filtro) {
        validarPeriodo(filtro);
        var spec = TransacaoSpecs.filtrar(atual.id(), filtro);
        long total = transacoes.count(spec);
        if (total > LIMITE_EXPORTACAO) {
            throw new RegraNegocioException("Exportação limitada a " + LIMITE_EXPORTACAO
                    + " registros; restrinja o período");
        }
        return transacoes.findAll(spec, ordenacaoPadrao());
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscar(Long id) {
        return TransacaoResponse.de(obter(id));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest req) {
        Long u = atual.id();
        Transacao t = new Transacao();
        t.setUsuario(usuarios.getReferenceById(u));
        aplicar(t, req, u);
        transacoes.save(t);
        return TransacaoResponse.de(t);
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest req) {
        Transacao t = obter(id);
        if (t.getTipo() == TipoTransacao.TRANSFERENCIA) {
            throw new RegraNegocioException("Transferências não podem ser editadas; exclua e crie novamente");
        }
        aplicar(t, req, atual.id());
        transacoes.flush();
        return TransacaoResponse.de(t);
    }

    @Transactional
    public TransacaoResponse transferir(TransferenciaRequest req) {
        Long u = atual.id();
        if (req.contaOrigemId().equals(req.contaDestinoId())) {
            throw new RegraNegocioException("Contas de origem e destino devem ser diferentes",
                    Map.of("contaDestinoId", "deve ser diferente da conta de origem"));
        }
        Transacao t = new Transacao();
        t.setUsuario(usuarios.getReferenceById(u));
        t.setDescricao(req.descricao().trim());
        t.setValor(req.valor());
        t.setTipo(TipoTransacao.TRANSFERENCIA);
        t.setSituacao(SituacaoTransacao.PAGA);
        t.setData(req.data());
        t.setObservacao(req.observacao());
        t.setConta(contaAtiva(req.contaOrigemId(), u, "contaOrigemId"));
        t.setContaDestino(contaAtiva(req.contaDestinoId(), u, "contaDestinoId"));
        transacoes.save(t);
        return TransacaoResponse.de(t);
    }

    @Transactional
    public void excluir(Long id) {
        transacoes.delete(obter(id));
    }

    private Transacao obter(Long id) {
        return transacoes.findByIdAndUsuarioId(id, atual.id())
                .orElseThrow(() -> new NaoEncontradoException("Transação não encontrada"));
    }

    private void aplicar(Transacao t, TransacaoRequest req, Long u) {
        if (req.tipo() == TipoTransacao.TRANSFERENCIA) {
            throw new RegraNegocioException("Use /transferencias para transferir entre contas",
                    Map.of("tipo", "deve ser RECEITA ou DESPESA"));
        }
        Categoria categoria = categorias.findVisivel(req.categoriaId(), u)
                .orElseThrow(() -> new RegraNegocioException("Categoria inválida",
                        Map.of("categoriaId", "categoria não encontrada")));
        if (!categoria.getTipo().name().equals(req.tipo().name())) {
            throw new RegraNegocioException("A categoria não é compatível com o tipo da transação",
                    Map.of("categoriaId", "categoria do tipo " + categoria.getTipo()));
        }
        Conta conta = t.getConta() != null && t.getConta().getId().equals(req.contaId())
                ? t.getConta() : contaAtiva(req.contaId(), u, "contaId");
        FormaPagamento forma = null;
        if (req.formaPagamentoId() != null) {
            forma = formas.findById(req.formaPagamentoId()).orElseThrow(() -> new RegraNegocioException(
                    "Forma de pagamento inválida", Map.of("formaPagamentoId", "forma não encontrada")));
        }
        t.setDescricao(req.descricao().trim());
        t.setValor(req.valor());
        t.setTipo(req.tipo());
        t.setSituacao(req.situacao() != null ? req.situacao() : SituacaoTransacao.PAGA);
        t.setData(req.data());
        t.setDataVencimento(req.dataVencimento());
        t.setObservacao(req.observacao() == null || req.observacao().isBlank() ? null : req.observacao().trim());
        t.setCategoria(categoria);
        t.setConta(conta);
        t.setFormaPagamento(forma);
        t.setEtiquetas(resolverEtiquetas(req.etiquetaIds(), u));
    }

    private Conta contaAtiva(Long id, Long u, String campo) {
        Conta c = contas.findByIdAndUsuarioId(id, u).orElseThrow(() -> new RegraNegocioException(
                "Conta inválida", Map.of(campo, "conta não encontrada")));
        if (!c.isAtiva()) {
            throw new RegraNegocioException("Conta inativa", Map.of(campo, "conta inativa"));
        }
        return c;
    }

    private Set<Etiqueta> resolverEtiquetas(Set<Long> ids, Long u) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Etiqueta> encontradas = etiquetas.findByIdInAndUsuarioId(ids, u);
        if (encontradas.size() != new HashSet<>(ids).size()) {
            throw new RegraNegocioException("Etiqueta inválida", Map.of("etiquetaIds", "etiqueta não encontrada"));
        }
        return new LinkedHashSet<>(encontradas);
    }

    private static void validarPeriodo(TransacaoFiltro f) {
        if (f.de() != null && f.ate() != null && f.de().isAfter(f.ate())) {
            throw new RegraNegocioException("Período inválido", Map.of("de", "deve ser anterior ou igual a 'ate'"));
        }
    }

    private static Sort ordenacaoPadrao() {
        return Sort.by(Sort.Order.desc("data"));
    }
}
