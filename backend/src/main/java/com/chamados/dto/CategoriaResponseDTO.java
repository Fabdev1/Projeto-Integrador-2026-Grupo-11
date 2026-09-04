package com.chamados.dto;

import com.chamados.domain.entity.Categoria;

public record CategoriaResponseDTO(Long id, String nome) {

    public static CategoriaResponseDTO de(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome());
    }
}
