package com.chamados.service;

import com.chamados.domain.entity.Categoria;
import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Comentario;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.PerfilUsuario;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.AlterarStatusRequestDTO;
import com.chamados.dto.ChamadoDetalheResponseDTO;
import com.chamados.dto.ChamadoRequestDTO;
import com.chamados.dto.ChamadoResponseDTO;
import com.chamados.dto.ResumoChamadosDTO;
import com.chamados.exception.RecursoNaoEncontradoException;
import com.chamados.exception.RegraDeNegocioException;
import com.chamados.repository.CategoriaRepository;
import com.chamados.repository.ChamadoRepository;
import com.chamados.repository.ComentarioRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras do ciclo de vida do chamado.
 *
 * Mudancas da 2a etapa em relacao a versao inicial:
 *   - toda transicao de status grava uma linha em historico_status, na mesma
 *     transacao (a tabela existia no modelo e nao era alimentada);
 *   - alterarStatus passou a exigir o autor da mudanca, porque a coluna
 *     historico_status.alterado_por e NOT NULL;
 *   - as transicoes invalidas devolvem 409 com mensagem legivel, em vez de
 *     aceitar qualquer valor do enum;
 *   - abertura e atribuicao geram notificacao;
 *   - listagem com filtros, detalhe completo e resumo para o painel.
 */
@Service
public class ChamadoService {

    private static final List<StatusChamado> STATUS_CONCLUIDOS =
            List.of(StatusChamado.RESOLVIDO, StatusChamado.FECHADO);

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final HistoricoStatusService historicoStatusService;
    private final NotificacaoService notificacaoService;
    private final ComentarioService comentarioService;
    private final AvaliacaoService avaliacaoService;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          CategoriaRepository categoriaRepository,
                          UsuarioRepository usuarioRepository,
                          ComentarioRepository comentarioRepository,
                          HistoricoStatusService historicoStatusService,
                          NotificacaoService notificacaoService,
                          ComentarioService comentarioService,
                          AvaliacaoService avaliacaoService) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.historicoStatusService = historicoStatusService;
        this.notificacaoService = notificacaoService;
        this.comentarioService = comentarioService;
        this.avaliacaoService = avaliacaoService;
    }

    // ------------------------------------------------------------------
    // Abertura
    // ------------------------------------------------------------------

    @Transactional
    public ChamadoResponseDTO criarChamado(ChamadoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", dto.getCategoriaId()));

        Usuario solicitante = usuarioRepository.findById(dto.getSolicitanteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitante", dto.getSolicitanteId()));

        Chamado chamado = Chamado.builder()
                .titulo(dto.getTitulo().trim())
                .descricao(dto.getDescricao())
                .categoria(categoria)
                .solicitante(solicitante)
                .prioridade(dto.getPrioridade())
                .status(StatusChamado.ABERTO)
                .build();

        chamado = chamadoRepository.save(chamado);

        // Primeiro evento da linha do tempo: nao existe status anterior.
        historicoStatusService.registrar(chamado, solicitante, null, StatusChamado.ABERTO);

        notificacaoService.notificarTecnicos(chamado,
                "Novo chamado #" + chamado.getId() + " (" + chamado.getPrioridade() + "): " + chamado.getTitulo());

        return new ChamadoResponseDTO(chamado);
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarTodos() {
        return listarComFiltros(null, null, null);
    }

    /**
     * Filtros opcionais e combinaveis. O volume da prova de conceito cabe em
     * memoria com folga, entao a consulta traz a lista com fetch join e o
     * recorte acontece aqui. Com volume de producao, isso viraria Specification
     * ou consulta paginada.
     */
    @Transactional(readOnly = true)
    public List<ChamadoResponseDTO> listarComFiltros(StatusChamado status,
                                                     Long solicitanteId,
                                                     Long tecnicoId) {
        return chamadoRepository.buscarTodosComRelacionamentos().stream()
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> solicitanteId == null
                        || (c.getSolicitante() != null && c.getSolicitante().getId().equals(solicitanteId)))
                .filter(c -> tecnicoId == null
                        || (c.getTecnico() != null && c.getTecnico().getId().equals(tecnicoId)))
                .map(ChamadoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChamadoResponseDTO buscarPorId(Long id) {
        return new ChamadoResponseDTO(buscarEntidade(id));
    }

    /** Chamado, linha do tempo, conversa e avaliacao em uma requisicao so. */
    @Transactional(readOnly = true)
    public ChamadoDetalheResponseDTO buscarDetalhes(Long id) {
        Chamado chamado = buscarEntidade(id);
        return new ChamadoDetalheResponseDTO(
                new ChamadoResponseDTO(chamado),
                historicoStatusService.listarPorChamado(id),
                comentarioService.listarPorChamado(id),
                avaliacaoService.buscarPorChamadoOuNulo(id));
    }

    @Transactional(readOnly = true)
    public ResumoChamadosDTO resumo() {
        return new ResumoChamadosDTO(
                chamadoRepository.countByStatus(StatusChamado.ABERTO),
                chamadoRepository.countByStatus(StatusChamado.EM_ANDAMENTO),
                chamadoRepository.countByStatusIn(STATUS_CONCLUIDOS),
                chamadoRepository.count());
    }

    // ------------------------------------------------------------------
    // Transicoes
    // ------------------------------------------------------------------

    @Transactional
    public ChamadoResponseDTO alterarStatus(Long id, AlterarStatusRequestDTO dto) {
        Chamado chamado = buscarEntidade(id);
        Usuario autor = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario", dto.usuarioId()));

        StatusChamado anterior = chamado.getStatus();
        StatusChamado novo = dto.statusNovo();

        validarTransicao(chamado, anterior, novo);

        chamado.setStatus(novo);
        chamado = chamadoRepository.save(chamado);

        historicoStatusService.registrar(chamado, autor, anterior, novo);

        if (dto.comentario() != null && !dto.comentario().isBlank()) {
            comentarioRepository.save(Comentario.builder()
                    .chamado(chamado)
                    .usuario(autor)
                    .mensagem(dto.comentario().trim())
                    .build());
        }

        notificacaoService.notificar(chamado.getSolicitante(), chamado,
                "O chamado #" + chamado.getId() + " passou para " + novo + ".");

        return new ChamadoResponseDTO(chamado);
    }

    @Transactional
    public ChamadoResponseDTO atribuirTecnico(Long chamadoId, Long tecnicoId) {
        Chamado chamado = buscarEntidade(chamadoId);

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tecnico", tecnicoId));

        if (tecnico.getPerfil() != PerfilUsuario.TECNICO && tecnico.getPerfil() != PerfilUsuario.ADMIN) {
            throw new RegraDeNegocioException(
                    tecnico.getNome() + " tem perfil " + tecnico.getPerfil() + " e nao pode assumir chamados.");
        }

        if (STATUS_CONCLUIDOS.contains(chamado.getStatus()) || chamado.getStatus() == StatusChamado.CANCELADO) {
            throw new RegraDeNegocioException("Chamado encerrado nao aceita troca de tecnico.");
        }

        chamado.setTecnico(tecnico);

        StatusChamado anterior = chamado.getStatus();
        if (anterior == StatusChamado.ABERTO) {
            chamado.setStatus(StatusChamado.EM_ANDAMENTO);
        }
        chamado = chamadoRepository.save(chamado);

        if (chamado.getStatus() != anterior) {
            historicoStatusService.registrar(chamado, tecnico, anterior, chamado.getStatus());
        }

        notificacaoService.notificar(chamado.getSolicitante(), chamado,
                "Seu chamado #" + chamado.getId() + " foi assumido por " + tecnico.getNome() + ".");

        return new ChamadoResponseDTO(chamado);
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private Chamado buscarEntidade(Long id) {
        return chamadoRepository.buscarPorIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado", id));
    }

    /**
     * Maquina de estados do chamado:
     *
     *   ABERTO -> EM_ANDAMENTO | CANCELADO
     *   EM_ANDAMENTO -> RESOLVIDO | CANCELADO
     *   RESOLVIDO -> EM_ANDAMENTO (reabertura) | FECHADO
     *   FECHADO, CANCELADO -> nada (estados finais)
     */
    private void validarTransicao(Chamado chamado, StatusChamado anterior, StatusChamado novo) {
        if (anterior == novo) {
            throw new RegraDeNegocioException("O chamado ja esta com o status " + novo + ".");
        }
        if (anterior == StatusChamado.FECHADO || anterior == StatusChamado.CANCELADO) {
            throw new RegraDeNegocioException(
                    "Chamado " + anterior.name().toLowerCase() + " nao muda mais de status.");
        }
        if (novo == StatusChamado.FECHADO) {
            throw new RegraDeNegocioException(
                    "O fechamento do chamado deve ser realizado exclusivamente pela avaliacao do atendimento.");
        }
        if (novo == StatusChamado.ABERTO) {
            throw new RegraDeNegocioException("O chamado nao pode regredir para o status aberto.");
        }
        if (novo == StatusChamado.EM_ANDAMENTO && chamado.getTecnico() == null) {
            throw new RegraDeNegocioException("Atribua um tecnico antes de colocar o chamado em andamento.");
        }
        if (novo == StatusChamado.RESOLVIDO && anterior != StatusChamado.EM_ANDAMENTO) {
            throw new RegraDeNegocioException("So um chamado em andamento pode ser marcado como resolvido.");
        }
        if (anterior == StatusChamado.RESOLVIDO && novo != StatusChamado.EM_ANDAMENTO) {
            throw new RegraDeNegocioException(
                    "Um chamado resolvido so pode ser reaberto (voltando para em andamento) ou avaliado.");
        }
    }
}
