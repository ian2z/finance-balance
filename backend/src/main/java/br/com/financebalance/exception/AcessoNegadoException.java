package br.com.financebalance.exception;

/** Recurso visivel ao usuario mas sem permissao de alteracao (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
