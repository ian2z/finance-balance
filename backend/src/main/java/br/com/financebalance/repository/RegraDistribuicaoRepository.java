package br.com.financebalance.repository;

import br.com.financebalance.entity.RegraDistribuicao;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegraDistribuicaoRepository extends JpaRepository<RegraDistribuicao, Long> {
    @EntityGraph(attributePaths = "categoria")
    List<RegraDistribuicao> findByUsuarioIdOrderByPercentualDesc(Long usuarioId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RegraDistribuicao r where r.usuario.id = :u")
    void apagarDoUsuario(@Param("u") Long usuarioId);
}
