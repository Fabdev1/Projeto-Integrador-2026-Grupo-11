package com.chamados.exception;

/**
 * Lancada quando a operacao e valida no formato mas conflita com o estado atual
 * do sistema (avaliar duas vezes o mesmo chamado, avaliar chamado em andamento,
 * atribuir um usuario que nao e tecnico, etc).
 * Tratada como HTTP 409 pelo GlobalExceptionHandler.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
