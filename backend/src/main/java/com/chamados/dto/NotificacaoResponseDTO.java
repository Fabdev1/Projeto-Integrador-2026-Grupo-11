package com.chamados.dto;

import com.chamados.domain.entity.Notificacao;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificacaoResponseDTO {

    private Long id;
    private Long chamadoId;
    private String mensagem;
    private Boolean lida;
    private LocalDateTime dataCriacao;

    public NotificacaoResponseDTO(Notificacao notificacao) {
        this.id = notificacao.getId();
        this.chamadoId = notificacao.getChamado().getId();
        this.mensagem = notificacao.getMensagem();
        this.lida = notificacao.getLida();
        this.dataCriacao = notificacao.getDataCriacao();
    }
}
