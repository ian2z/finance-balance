package br.com.financebalance.controller;

import br.com.financebalance.dto.PaginaResponse;
import br.com.financebalance.dto.TransacaoDtos.TransacaoRequest;
import br.com.financebalance.dto.TransacaoDtos.TransacaoResponse;
import br.com.financebalance.dto.TransacaoDtos.TransferenciaRequest;
import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.service.TransacaoService;
import br.com.financebalance.specification.TransacaoFiltro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Transações")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping("/transacoes")
    @Operation(summary = "Lista paginada com filtros opcionais (ordenação: data, valor, descricao, criadoEm)")
    public PaginaResponse<TransacaoResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) SituacaoTransacao situacao,
            @RequestParam(required = false) Long etiquetaId,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate,
            Pageable pageable) {
        return service.listar(new TransacaoFiltro(texto, categoriaId, contaId, tipo, situacao, etiquetaId, de, ate),
                pageable);
    }

    @GetMapping("/transacoes/{id}")
    public TransacaoResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping("/transacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse criar(@Valid @RequestBody TransacaoRequest req) {
        return service.criar(req);
    }

    @PutMapping("/transacoes/{id}")
    public TransacaoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/transacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transferência entre contas do usuário (não conta como receita nem despesa)")
    public TransacaoResponse transferir(@Valid @RequestBody TransferenciaRequest req) {
        return service.transferir(req);
    }
}
