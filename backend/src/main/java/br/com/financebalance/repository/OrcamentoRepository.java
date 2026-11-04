package br.com.financebalance.repository;

import br.com.financebalance.entity.Orcamento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    @EntityGraph(attributePaths = "categoria")
    List<Orcamento> findByUsuarioIdAndMesOrderByCategoriaNomeAsc(Long usuarioId, LocalDate mes);

    @EntityGraph(attributePaths = "categoria")
    Optional<Orcamento> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndCategoriaIdAndMes(Long usuarioId, Long categoriaId, LocalDate mes);

    boolean existsByUsuarioIdAndCategoriaIdAndMesAndIdNot(Long usuarioId, Long categoriaId, LocalDate mes, Long id);
}
