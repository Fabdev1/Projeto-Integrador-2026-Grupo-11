package com.chamados.service;

import com.chamados.dto.CategoriaResponseDTO;
import com.chamados.repository.CategoriaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Alimenta o campo de categoria do formulario. A interface passou a montar
     * esse select a partir do banco, e nao mais de uma lista fixa no HTML, para
     * que o id enviado exista de fato em categorias.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaResponseDTO::de)
                .toList();
    }
}
