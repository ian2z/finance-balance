package br.com.financebalance.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErroResponse("Dados inválidos", campos));
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<ErroResponse> regra(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(new ErroResponse(ex.getMessage(), ex.getCampos()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ErroResponse> requisicaoInvalida(Exception ex) {
        return ResponseEntity.badRequest().body(ErroResponse.de("Requisição inválida"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErroResponse> credenciais(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroResponse.de("E-mail ou senha inválidos"));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    ResponseEntity<ErroResponse> negado(AcessoNegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErroResponse.de(ex.getMessage()));
    }

    @ExceptionHandler({NaoEncontradoException.class, NoResourceFoundException.class})
    ResponseEntity<ErroResponse> naoEncontrado(Exception ex) {
        String msg = ex instanceof NaoEncontradoException ? ex.getMessage() : "Recurso não encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse.de(msg));
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.de(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.de("Operação conflita com dados existentes"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErroResponse> inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErroResponse.de("Erro interno do servidor"));
    }
}
