package br.com.financebalance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExportacaoServiceTest {

    @Test
    void escapaSeparadorEAspas() {
        assertEquals("\"a;b\"", ExportacaoService.campo("a;b"));
        assertEquals("\"diz \"\"oi\"\"\"", ExportacaoService.campo("diz \"oi\""));
    }

    @Test
    void neutralizaInjecaoDeFormula() {
        assertEquals("'=1+1", ExportacaoService.campo("=1+1"));
        assertEquals("'@SUM(A1)", ExportacaoService.campo("@SUM(A1)"));
        assertEquals("'-5", ExportacaoService.campo("-5"));
        assertEquals("normal", ExportacaoService.campo("normal"));
        assertEquals("", ExportacaoService.campo(null));
    }
}
