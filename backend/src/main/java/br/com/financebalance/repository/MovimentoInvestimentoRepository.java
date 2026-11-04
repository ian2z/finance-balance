package br.com.financebalance.repository;

import br.com.financebalance.entity.MovimentoInvestimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentoInvestimentoRepository extends JpaRepository<MovimentoInvestimento, Long> {
    List<MovimentoInvestimento> findByInvestimentoIdOrderByDataDescIdDesc(Long investimentoId);
}
