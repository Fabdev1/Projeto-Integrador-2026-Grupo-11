package com.chamados.dto;

import com.chamados.domain.entity.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        Long chamadoId,
        String tituloChamado,
        String mensagem,
        Boolean lida,
        LocalDateTime dataCriacao
) {
    public static NotificacaoResponseDTO de(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getChamado().getId(),
                notificacao.getChamado().getTitulo(),
                notificacao.getMensagem(),
                notificacao.getLida(),
                notificacao.getDataCriacao());
    }
}
