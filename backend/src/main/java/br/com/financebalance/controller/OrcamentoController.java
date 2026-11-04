package br.com.financebalance.controller;

import br.com.financebalance.dto.OrcamentoDtos.OrcamentoRequest;
import br.com.financebalance.dto.OrcamentoDtos.OrcamentoResponse;
import br.com.financebalance.service.OrcamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orcamentos")
@Tag(name = "Orçamentos")
public class OrcamentoController {

    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrcamentoResponse> listar(@RequestParam(required = false) LocalDate mes) {
        return service.listar(mes);
    }

    @GetMapping("/{id}")
    public OrcamentoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrcamentoResponse criar(@Valid @RequestBody OrcamentoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public OrcamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody OrcamentoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
