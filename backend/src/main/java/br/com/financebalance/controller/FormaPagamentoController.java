package br.com.financebalance.controller;

import br.com.financebalance.dto.RefDto;
import br.com.financebalance.service.FormaPagamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/formas-pagamento")
@Tag(name = "Formas de pagamento")
public class FormaPagamentoController {

    private final FormaPagamentoService service;

    public FormaPagamentoController(FormaPagamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<RefDto> listar() {
        return service.listar();
    }
}
