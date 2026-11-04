package br.com.financebalance.controller;

import br.com.financebalance.entity.SituacaoTransacao;
import br.com.financebalance.entity.TipoTransacao;
import br.com.financebalance.service.ExportacaoService;
import br.com.financebalance.specification.TransacaoFiltro;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exportacao")
@Tag(name = "Exportação")
public class ExportacaoController {

    private final ExportacaoService service;

    public ExportacaoController(ExportacaoService service) {
        this.service = service;
    }

    @GetMapping("/transacoes.csv")
    public ResponseEntity<byte[]> transacoes(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long contaId,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) SituacaoTransacao situacao,
            @RequestParam(required = false) Long etiquetaId,
            @RequestParam(required = false) LocalDate de,
            @RequestParam(required = false) LocalDate ate) {
        byte[] csv = service.transacoesCsv(
                new TransacaoFiltro(texto, categoriaId, contaId, tipo, situacao, etiquetaId, de, ate));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("transacoes.csv").build().toString())
                .body(csv);
    }
}
