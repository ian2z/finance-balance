package br.com.financebalance.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** Formato unico de erro para web e mobile. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResponse(String mensagem, Map<String, String> campos) {
    public static ErroResponse de(String mensagem) {
        return new ErroResponse(mensagem, Map.of());
    }
}
