package com.chamados.dto;

import com.chamados.domain.entity.Comentario;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComentarioResponseDTO {

    private Long id;
    private Long chamadoId;
    private String nomeUsuario;
    private String mensagem;
    private LocalDateTime dataCriacao;

    public ComentarioResponseDTO(Comentario comentario) {
        this.id = comentario.getId();
        this.chamadoId = comentario.getChamado().getId();
        this.nomeUsuario = comentario.getUsuario().getNome();
        this.mensagem = comentario.getMensagem();
        this.dataCriacao = comentario.getDataCriacao();
    }
}
