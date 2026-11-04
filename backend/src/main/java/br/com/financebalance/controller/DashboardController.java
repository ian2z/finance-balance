package br.com.financebalance.controller;

import br.com.financebalance.dto.DashboardDtos.ResumoResponse;
import br.com.financebalance.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Totais e dados para gráficos (período padrão: mês atual)")
    public ResumoResponse resumo(@RequestParam(required = false) LocalDate de,
                                 @RequestParam(required = false) LocalDate ate) {
        return service.resumo(de, ate);
    }
}
