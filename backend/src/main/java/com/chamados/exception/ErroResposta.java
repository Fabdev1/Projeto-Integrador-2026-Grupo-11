package com.chamados.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato unico de erro devolvido pela API. O frontend le sempre o campo
 * "mensagem" para exibir ao usuario, e "campos" quando a falha e de validacao.
 */
public record ErroResposta(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
    public static ErroResposta de(int status, String erro, String mensagem, String caminho) {
        return new ErroResposta(LocalDateTime.now(), status, erro, mensagem, caminho, null);
    }

    public static ErroResposta deValidacao(String caminho, Map<String, String> campos) {
        return new ErroResposta(
                LocalDateTime.now(),
                400,
                "Requisicao invalida",
                "Confira os campos destacados.",
                caminho,
                campos
        );
    }
}
