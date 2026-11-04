package br.com.financebalance.exception;

/** Recurso inexistente ou pertencente a outro usuario (HTTP 404, para nao vazar existencia). */
public class NaoEncontradoException extends RuntimeException {
    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
