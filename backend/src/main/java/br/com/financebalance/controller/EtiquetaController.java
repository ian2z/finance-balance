package br.com.financebalance.controller;

import br.com.financebalance.dto.EtiquetaDtos.EtiquetaRequest;
import br.com.financebalance.dto.RefDto;
import br.com.financebalance.service.EtiquetaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/etiquetas")
@Tag(name = "Etiquetas")
public class EtiquetaController {

    private final EtiquetaService service;

    public EtiquetaController(EtiquetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<RefDto> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefDto criar(@Valid @RequestBody EtiquetaRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    public RefDto atualizar(@PathVariable Long id, @Valid @RequestBody EtiquetaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
