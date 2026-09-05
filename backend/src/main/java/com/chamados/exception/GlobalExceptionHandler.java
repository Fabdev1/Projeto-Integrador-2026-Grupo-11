package com.chamados.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centraliza a traducao de excecao para status HTTP.
 *
 * Sem esta classe, "chamado nao encontrado" chegava ao navegador como 500 com
 * stack trace, e a interface nao tinha como distinguir erro do usuario de erro
 * do servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex,
                                                            HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErroResposta.de(404, "Registro nao encontrado", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException ex,
                                                             HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResposta.de(409, "Operacao nao permitida", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex,
                                                        HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.put(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ErroResposta.deValidacao(req.getRequestURI(), campos));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ErroResposta> tratarArgumentoInvalido(Exception ex, HttpServletRequest req) {
        String mensagem = (ex instanceof HttpMessageNotReadableException)
                ? "Corpo da requisicao invalido ou valor de campo/enum nao reconhecido."
                : ex.getMessage();
        return ResponseEntity.badRequest().body(
                ErroResposta.de(400, "Requisicao invalida", mensagem, req.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException ex,
                                                          HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResposta.de(
                409,
                "Conflito de dados",
                "A operacao fere uma restricao do banco de dados (chave unica ou chave estrangeira).",
                req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarInesperado(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErroResposta.de(
                500,
                "Erro interno",
                "Falha inesperada no servidor. Consulte o log da aplicacao.",
                req.getRequestURI()));
    }
}
