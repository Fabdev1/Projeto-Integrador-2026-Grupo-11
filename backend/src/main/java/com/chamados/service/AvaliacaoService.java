package com.chamados.service;

import com.chamados.domain.entity.Avaliacao;
import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Usuario;
import com.chamados.dto.AvaliacaoRequestDTO;
import com.chamados.dto.AvaliacaoResponseDTO;
import com.chamados.repository.AvaliacaoRepository;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public AvaliacaoResponseDTO criar(AvaliacaoRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(dto.getChamadoId())
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + dto.getChamadoId()));

        if (avaliacaoRepository.findByChamadoId(dto.getChamadoId()).isPresent()) {
            throw new IllegalStateException("Este chamado já possui uma avaliação registrada");
        }

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + dto.getUsuarioId()));

        Avaliacao avaliacao = Avaliacao.builder()
                .chamado(chamado)
                .usuario(usuario)
                .nota(dto.getNota())
                .comentario(dto.getComentario())
                .build();

        avaliacao = avaliacaoRepository.save(avaliacao);
        return new AvaliacaoResponseDTO(avaliacao);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorChamado(Long chamadoId) {
        Avaliacao avaliacao = avaliacaoRepository.findByChamadoId(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma avaliação encontrada para o chamado ID: " + chamadoId));
        return new AvaliacaoResponseDTO(avaliacao);
    }
}
