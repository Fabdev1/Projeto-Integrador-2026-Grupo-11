package com.chamados.exception;

/**
 * Lancada quando um registro referenciado por id nao existe no banco.
 * Tratada como HTTP 404 pelo GlobalExceptionHandler.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " nao encontrado com ID: " + id);
    }
}
