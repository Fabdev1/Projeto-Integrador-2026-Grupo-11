package com.chamados.service;

import com.chamados.domain.entity.Categoria;
import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.HistoricoStatus;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.ChamadoRequestDTO;
import com.chamados.dto.ChamadoResponseDTO;
import com.chamados.repository.CategoriaRepository;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.HistoricoStatusRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoStatusRepository historicoStatusRepository;

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada com ID: " + dto.getCategoriaId()));

        Usuario solicitante = usuarioRepository.findById(dto.getSolicitanteId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitante não encontrado com ID: " + dto.getSolicitanteId()));

        Chamado chamado = Chamado.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .categoria(categoria)
                .solicitante(solicitante)
                .prioridade(dto.getPrioridade())
                .status(StatusChamado.ABERTO)
                .build();

        chamado = chamadoRepository.save(chamado);
        return new ChamadoResponseDTO(chamado);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponseDTO> listarTodos(Long solicitanteId, Pageable pageable) {
        Page<Chamado> chamados = solicitanteId != null
                ? chamadoRepository.findBySolicitanteId(solicitanteId, pageable)
                : chamadoRepository.findAll(pageable);

        return chamados.map(ChamadoResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public ChamadoResponseDTO buscarPorId(Long id) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + id));
        return new ChamadoResponseDTO(chamado);
    }

    @Transactional
    public ChamadoResponseDTO alterarStatus(Long id, StatusChamado novoStatus, Long alteradoPorId) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + id));

        Usuario alteradoPor = usuarioRepository.findById(alteradoPorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com ID: " + alteradoPorId));

        StatusChamado statusAnterior = chamado.getStatus();
        chamado.setStatus(novoStatus);
        chamado = chamadoRepository.save(chamado);

        HistoricoStatus historico = HistoricoStatus.builder()
                .chamado(chamado)
                .alteradoPor(alteradoPor)
                .statusAnterior(statusAnterior)
                .statusNovo(novoStatus)
                .build();
        historicoStatusRepository.save(historico);

        return new ChamadoResponseDTO(chamado);
    }

    @Transactional
    public ChamadoResponseDTO atribuirTecnico(Long chamadoId, Long tecnicoId) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new IllegalArgumentException("Chamado não encontrado com ID: " + chamadoId));

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new IllegalArgumentException("Técnico não encontrado com ID: " + tecnicoId));

        chamado.setTecnico(tecnico);
        if (chamado.getStatus() == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }

        chamado = chamadoRepository.save(chamado);
        return new ChamadoResponseDTO(chamado);
    }
}
