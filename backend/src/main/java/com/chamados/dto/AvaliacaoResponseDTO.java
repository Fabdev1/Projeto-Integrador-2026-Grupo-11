package com.chamados.dto;

import com.chamados.domain.entity.Avaliacao;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AvaliacaoResponseDTO {

    private Long id;
    private Long chamadoId;
    private String nomeUsuario;
    private Integer nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;

    public AvaliacaoResponseDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.chamadoId = avaliacao.getChamado().getId();
        this.nomeUsuario = avaliacao.getUsuario().getNome();
        this.nota = avaliacao.getNota();
        this.comentario = avaliacao.getComentario();
        this.dataAvaliacao = avaliacao.getDataAvaliacao();
    }
}
