package com.chamados.service;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Comentario;
import com.chamados.domain.entity.Usuario;
import com.chamados.dto.ComentarioRequestDTO;
import com.chamados.dto.ComentarioResponseDTO;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.ComentarioRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public ComentarioResponseDTO criar(ComentarioRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(dto.getChamadoId())
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + dto.getChamadoId()));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + dto.getUsuarioId()));

        Comentario comentario = Comentario.builder()
                .chamado(chamado)
                .usuario(usuario)
                .mensagem(dto.getMensagem())
                .build();

        comentario = comentarioRepository.save(comentario);
        return new ComentarioResponseDTO(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarPorChamado(Long chamadoId) {
        return comentarioRepository.findByChamadoIdOrderByDataCriacaoAsc(chamadoId).stream()
                .map(ComentarioResponseDTO::new)
                .collect(Collectors.toList());
    }
}
