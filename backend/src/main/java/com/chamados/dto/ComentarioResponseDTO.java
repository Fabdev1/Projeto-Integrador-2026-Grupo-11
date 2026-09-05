package com.chamados.dto;

import com.chamados.domain.entity.Comentario;
import com.chamados.domain.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Long id,
        Long chamadoId,
        Long autorId,
        String autorNome,
        PerfilUsuario autorPerfil,
        String mensagem,
        LocalDateTime dataCriacao
) {
    public static ComentarioResponseDTO de(Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getChamado().getId(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNome(),
                comentario.getUsuario().getPerfil(),
                comentario.getMensagem(),
                comentario.getDataCriacao());
    }
}
