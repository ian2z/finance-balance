package br.com.financebalance.repository;

import br.com.financebalance.entity.FormaPagamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
    List<FormaPagamento> findAllByOrderByNomeAsc();
}
