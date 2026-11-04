package br.com.financebalance.service;

import br.com.financebalance.dto.RefDto;
import br.com.financebalance.repository.FormaPagamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormaPagamentoService {

    private final FormaPagamentoRepository formas;

    public FormaPagamentoService(FormaPagamentoRepository formas) {
        this.formas = formas;
    }

    @Transactional(readOnly = true)
    public List<RefDto> listar() {
        return formas.findAllByOrderByNomeAsc().stream().map(f -> new RefDto(f.getId(), f.getNome())).toList();
    }
}
