package br.com.financebalance.controller;

import br.com.financebalance.dto.DistribuicaoDtos.DistribuicaoRequest;
import br.com.financebalance.dto.DistribuicaoDtos.DistribuicaoResponse;
import br.com.financebalance.service.DistribuicaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distribuicao")
@Tag(name = "Distribuição da renda")
public class DistribuicaoController {

    private final DistribuicaoService service;

    public DistribuicaoController(DistribuicaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Distribuição planejada versus realizada no mês informado (padrão: mês atual)")
    public DistribuicaoResponse consultar(@RequestParam(required = false) LocalDate mes) {
        return service.consultar(mes);
    }

    @PutMapping
    @Operation(summary = "Define a renda base e substitui as regras percentuais (soma máxima de 100%)")
    public DistribuicaoResponse definir(@Valid @RequestBody DistribuicaoRequest req) {
        return service.definir(req);
    }
}
