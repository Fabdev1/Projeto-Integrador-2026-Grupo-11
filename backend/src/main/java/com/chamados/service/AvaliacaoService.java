package com.chamados.service;

import com.chamados.domain.entity.Avaliacao;
import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.AvaliacaoRequestDTO;
import com.chamados.dto.AvaliacaoResponseDTO;
import com.chamados.exception.RecursoNaoEncontradoException;
import com.chamados.exception.RegraDeNegocioException;
import com.chamados.repository.AvaliacaoRepository;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fechamento do ciclo: o solicitante avalia o atendimento e o chamado vai para
 * FECHADO.
 *
 * Tres regras sustentam o relacionamento 1:1 desenhado na primeira etapa:
 *   1. so da para avaliar chamado RESOLVIDO;
 *   2. so o solicitante do chamado avalia;
 *   3. uma avaliacao por chamado (o banco tambem garante, com UNIQUE em
 *      avaliacoes.chamado_id; a checagem aqui existe para devolver 409 com
 *      mensagem legivel em vez de erro de integridade).
 */
@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoStatusService historicoStatusService;
    private final NotificacaoService notificacaoService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            ChamadoRepository chamadoRepository,
                            UsuarioRepository usuarioRepository,
                            HistoricoStatusService historicoStatusService,
                            NotificacaoService notificacaoService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoStatusService = historicoStatusService;
        this.notificacaoService = notificacaoService;
    }

    @Transactional
    public AvaliacaoResponseDTO avaliar(Long chamadoId, AvaliacaoRequestDTO dto) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado", chamadoId));

        if (chamado.getStatus() != StatusChamado.RESOLVIDO) {
            throw new RegraDeNegocioException(
                    "So e possivel avaliar um chamado resolvido. Status atual: " + chamado.getStatus() + ".");
        }

        if (avaliacaoRepository.existsByChamadoId(chamadoId)) {
            throw new RegraDeNegocioException("Este chamado ja foi avaliado.");
        }

        Usuario autor = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", dto.usuarioId()));

        if (!chamado.getSolicitante().getId().equals(autor.getId())) {
            throw new RegraDeNegocioException("A avaliacao e de quem abriu o chamado.");
        }

        Avaliacao avaliacao = avaliacaoRepository.save(Avaliacao.builder()
                .chamado(chamado)
                .usuario(autor)
                .nota(dto.nota())
                .comentario(dto.comentario())
                .build());

        StatusChamado anterior = chamado.getStatus();
        chamado.setStatus(StatusChamado.FECHADO);
        chamadoRepository.save(chamado);
        historicoStatusService.registrar(chamado, autor, anterior, StatusChamado.FECHADO);

        notificacaoService.notificar(
                chamado.getTecnico(),
                chamado,
                "O chamado #" + chamado.getId() + " foi avaliado com nota " + dto.nota() + " e encerrado.");

        return AvaliacaoResponseDTO.de(avaliacao);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorChamado(Long chamadoId) {
        return avaliacaoRepository.findByChamadoId(chamadoId)
                .map(AvaliacaoResponseDTO::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Este chamado ainda nao tem avaliacao."));
    }

    /** Usada pela tela de detalhes, onde a ausencia de avaliacao e normal. */
    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorChamadoOuNulo(Long chamadoId) {
        return avaliacaoRepository.findByChamadoId(chamadoId)
                .map(AvaliacaoResponseDTO::de)
                .orElse(null);
    }
}
