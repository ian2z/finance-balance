package br.com.financebalance.repository;

import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.entity.Transacao;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransacaoRepository extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {

    String GRAFO = "categoria,conta,contaDestino,formaPagamento";

    @EntityGraph(attributePaths = {"categoria", "conta", "contaDestino", "formaPagamento"})
    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Override
    @EntityGraph(attributePaths = {"categoria", "conta", "contaDestino", "formaPagamento"})
    Page<Transacao> findAll(Specification<Transacao> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"categoria", "conta", "contaDestino", "formaPagamento"})
    List<Transacao> findAll(Specification<Transacao> spec, org.springframework.data.domain.Sort sort);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaIdOrContaDestinoId(Long contaId, Long contaDestinoId);

    /** Linhas [tipo, situacao, soma] das receitas e despesas do periodo (transferencias ficam de fora). */
    @Query("select t.tipo, t.situacao, sum(t.valor) from Transacao t "
            + "where t.usuario.id = :u and t.tipo <> :transferencia and t.data between :de and :ate "
            + "group by t.tipo, t.situacao")
    List<Object[]> totaisPorTipoESituacao(@Param("u") Long usuarioId, @Param("de") LocalDate de,
                                          @Param("ate") LocalDate ate,
                                          @Param("transferencia") TipoTransacao transferencia);

    /** Linhas [categoriaId, nome, soma] agrupadas pela categoria de nivel superior. */
    @Query("select coalesce(p.id, c.id), coalesce(p.nome, c.nome), sum(t.valor) "
            + "from Transacao t join t.categoria c left join c.categoriaPai p "
            + "where t.usuario.id = :u and t.tipo = :tipo and t.situacao = :situacao "
            + "and t.data between :de and :ate "
            + "group by coalesce(p.id, c.id), coalesce(p.nome, c.nome) "
            + "order by sum(t.valor) desc")
    List<Object[]> totaisPorCategoria(@Param("u") Long usuarioId, @Param("tipo") TipoTransacao tipo,
                                      @Param("situacao") SituacaoTransacao situacao,
                                      @Param("de") LocalDate de, @Param("ate") LocalDate ate);

    /** Linhas [ano, mes, tipo, soma] das transacoes pagas. */
    @Query("select year(t.data), month(t.data), t.tipo, sum(t.valor) from Transacao t "
            + "where t.usuario.id = :u and t.tipo <> :transferencia and t.situacao = :paga "
            + "and t.data between :de and :ate group by year(t.data), month(t.data), t.tipo")
    List<Object[]> totaisMensais(@Param("u") Long usuarioId, @Param("de") LocalDate de,
                                 @Param("ate") LocalDate ate, @Param("paga") SituacaoTransacao paga,
                                 @Param("transferencia") TipoTransacao transferencia);

    @EntityGraph(attributePaths = {"categoria", "conta"})
    @Query("select t from Transacao t where t.usuario.id = :u and t.tipo = :tipo and t.situacao = :situacao "
            + "and t.data between :de and :ate order by t.valor desc, t.id desc")
    List<Transacao> maioresValores(@Param("u") Long usuarioId, @Param("tipo") TipoTransacao tipo,
                                   @Param("situacao") SituacaoTransacao situacao,
                                   @Param("de") LocalDate de, @Param("ate") LocalDate ate, Pageable pageable);

    /** Linhas [contaId, tipo, soma] das transacoes pagas, pela conta de origem. */
    @Query("select t.conta.id, t.tipo, sum(t.valor) from Transacao t "
            + "where t.usuario.id = :u and t.situacao = :paga group by t.conta.id, t.tipo")
    List<Object[]> movimentosPorConta(@Param("u") Long usuarioId, @Param("paga") SituacaoTransacao paga);

    /** Linhas [contaDestinoId, soma] das transferencias pagas recebidas. */
    @Query("select t.contaDestino.id, sum(t.valor) from Transacao t "
            + "where t.usuario.id = :u and t.tipo = :transferencia and t.situacao = :paga "
            + "group by t.contaDestino.id")
    List<Object[]> entradasPorConta(@Param("u") Long usuarioId, @Param("paga") SituacaoTransacao paga,
                                    @Param("transferencia") TipoTransacao transferencia);

    /** Linhas [categoriaId, quantidade] para identificar categorias sem movimentacoes. */
    @Query("select t.categoria.id, count(t) from Transacao t where t.usuario.id = :u and t.categoria is not null "
            + "group by t.categoria.id")
    List<Object[]> contagemPorCategoria(@Param("u") Long usuarioId);

    /** Despesas (incluindo as de subcategorias) de uma categoria em um periodo. */
    @Query("select coalesce(sum(t.valor), 0) from Transacao t join t.categoria c left join c.categoriaPai p "
            + "where t.usuario.id = :u and t.tipo = :despesa and t.data between :de and :ate "
            + "and (c.id = :categoriaId or p.id = :categoriaId)")
    BigDecimal somaDespesasDaCategoria(@Param("u") Long usuarioId, @Param("categoriaId") Long categoriaId,
                                       @Param("despesa") TipoTransacao despesa,
                                       @Param("de") LocalDate de, @Param("ate") LocalDate ate);
}
