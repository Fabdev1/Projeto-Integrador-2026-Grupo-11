package com.chamados.dto;

import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.PerfilUsuario;

public record UsuarioResponseDTO(Long id, String nome, String email, PerfilUsuario perfil) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil());
    }
}
