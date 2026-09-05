package com.chamados.dto;

import com.chamados.domain.entity.Categoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponseDTO {

    private Long id;
    private String nome;

    public CategoriaResponseDTO(Categoria categoria) {
        this.id = categoria.getId();
        this.nome = categoria.getNome();
    }
}
