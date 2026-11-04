package br.com.financebalance.repository;

import br.com.financebalance.entity.Investimento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestimentoRepository extends JpaRepository<Investimento, Long> {
    List<Investimento> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Investimento> findByIdAndUsuarioId(Long id, Long usuarioId);
}
