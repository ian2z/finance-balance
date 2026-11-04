package br.com.financebalance.controller;

import br.com.financebalance.dto.InvestimentoDtos.*;
import br.com.financebalance.service.InvestimentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/investimentos")
@Tag(name = "Investimentos")
public class InvestimentoController {

    private final InvestimentoService service;

    public InvestimentoController(InvestimentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InvestimentoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/resumo")
    public ResumoCarteira resumo() {
        return service.resumo();
    }

    @GetMapping("/{id}")
    public InvestimentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestimentoResponse criar(@Valid @RequestBody InvestimentoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public InvestimentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody InvestimentoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @GetMapping("/{id}/movimentos")
    public List<MovimentoResponse> movimentos(@PathVariable Long id) {
        return service.listarMovimentos(id);
    }

    @PostMapping("/{id}/movimentos")
    @ResponseStatus(HttpStatus.CREATED)
    public InvestimentoResponse movimentar(@PathVariable Long id, @Valid @RequestBody MovimentoRequest req) {
        return service.movimentar(id, req);
    }
}
