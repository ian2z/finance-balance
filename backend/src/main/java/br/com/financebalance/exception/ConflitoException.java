package br.com.financebalance.exception;

/** Conflito de estado ou unicidade (HTTP 409). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
