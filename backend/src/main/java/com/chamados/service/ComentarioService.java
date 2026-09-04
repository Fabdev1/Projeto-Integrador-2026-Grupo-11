package com.chamados.service;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Comentario;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.ComentarioRequestDTO;
import com.chamados.dto.ComentarioResponseDTO;
import com.chamados.exception.RecursoNaoEncontradoException;
import com.chamados.exception.RegraDeNegocioException;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.ComentarioRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Conversa dentro do chamado, entre solicitante e tecnico.
 */
@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             ChamadoRepository chamadoRepository,
                             UsuarioRepository usuarioRepository,
                             NotificacaoService notificacaoService) {
        this.comentarioRepository = comentarioRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public ComentarioResponseDTO comentar(Long chamadoId, ComentarioRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado", chamadoId));

        if (chamado.getStatus() == StatusChamado.FECHADO || chamado.getStatus() == StatusChamado.CANCELADO) {
            throw new RegraDeNegocioException(
                    "Chamado " + chamado.getStatus().name().toLowerCase() + " nao aceita novos comentarios.");
        }

        Usuario autor = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", dto.usuarioId()));

        Comentario comentario = comentarioRepository.save(Comentario.builder()
                .chamado(chamado)
                .usuario(autor)
                .mensagem(dto.mensagem().trim())
                .build());

        avisarOutraParte(chamado, autor);

        return ComentarioResponseDTO.de(comentario);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarPorChamado(Long chamadoId) {
        if (!chamadoRepository.existsById(chamadoId)) {
            throw new RecursoNaoEncontradoException("Chamado", chamadoId);
        }
        return comentarioRepository.buscarPorChamado(chamadoId).stream()
                .map(ComentarioResponseDTO::de)
                .toList();
    }

    /**
     * Quem nao escreveu recebe o aviso. Se o tecnico comentou, avisa o
     * solicitante; se o solicitante comentou, avisa o tecnico responsavel.
     */
    private void avisarOutraParte(Chamado chamado, Usuario autor) {
        String mensagem = "Nova mensagem no chamado #" + chamado.getId() + " de " + autor.getNome() + ".";

        Usuario solicitante = chamado.getSolicitante();
        Usuario tecnico = chamado.getTecnico();

        if (solicitante != null && !solicitante.getId().equals(autor.getId())) {
            notificacaoService.notificar(solicitante, chamado, mensagem);
        }
        if (tecnico != null && !tecnico.getId().equals(autor.getId())) {
            notificacaoService.notificar(tecnico, chamado, mensagem);
        }
    }
}
