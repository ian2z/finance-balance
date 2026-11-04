package br.com.financebalance.service;

import br.com.financebalance.entity.Transacao;
import br.com.financebalance.specification.TransacaoFiltro;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Exportacao CSV (separador ';' e BOM UTF-8, para abrir corretamente no Excel pt-BR). */
@Service
public class ExportacaoService {

    private static final String CABECALHO = "id;data;tipo;situacao;descricao;valor;categoria;conta;conta_destino;"
            + "forma_pagamento;etiquetas;observacao";

    private final TransacaoService transacoes;

    public ExportacaoService(TransacaoService transacoes) {
        this.transacoes = transacoes;
    }

    @Transactional(readOnly = true)
    public byte[] transacoesCsv(TransacaoFiltro filtro) {
        StringBuilder sb = new StringBuilder("﻿").append(CABECALHO).append("\r\n");
        for (Transacao t : transacoes.listarParaExportacao(filtro)) {
            sb.append(String.join(";",
                    String.valueOf(t.getId()),
                    String.valueOf(t.getData()),
                    t.getTipo().name(),
                    t.getSituacao().name(),
                    campo(t.getDescricao()),
                    t.getValor().toPlainString(),
                    campo(t.getCategoria() == null ? "" : t.getCategoria().getNome()),
                    campo(t.getConta().getNome()),
                    campo(t.getContaDestino() == null ? "" : t.getContaDestino().getNome()),
                    campo(t.getFormaPagamento() == null ? "" : t.getFormaPagamento().getNome()),
                    campo(t.getEtiquetas().stream().map(e -> e.getNome()).sorted().collect(Collectors.joining(","))),
                    campo(t.getObservacao() == null ? "" : t.getObservacao())))
                    .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Escapa o campo e neutraliza formulas (CSV injection) prefixando com apostrofo. */
    static String campo(String valor) {
        String v = valor == null ? "" : valor;
        if (!v.isEmpty() && "=+-@\t\r".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            v = "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
