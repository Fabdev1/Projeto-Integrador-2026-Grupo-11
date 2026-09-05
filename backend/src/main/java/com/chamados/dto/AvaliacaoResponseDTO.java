package com.chamados.dto;

import com.chamados.domain.entity.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponseDTO(
        Long id,
        Long chamadoId,
        Integer nota,
        String comentario,
        String autorNome,
        LocalDateTime dataAvaliacao
) {
    public static AvaliacaoResponseDTO de(Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
                avaliacao.getId(),
                avaliacao.getChamado().getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getUsuario().getNome(),
                avaliacao.getDataAvaliacao());
    }
}
