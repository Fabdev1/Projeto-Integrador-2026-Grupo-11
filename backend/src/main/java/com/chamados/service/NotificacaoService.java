package com.chamados.service;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.Notificacao;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.PerfilUsuario;
import com.chamados.dto.NotificacaoResponseDTO;
import com.chamados.exception.RecursoNaoEncontradoException;
import com.chamados.repository.NotificacaoRepository;
import com.chamados.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Avisos internos. O envio por e-mail ficou fora do recorte da prova de
 * conceito: a notificacao e gravada na tabela notificacoes e lida pela interface.
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void notificar(Usuario destinatario, Chamado chamado, String mensagem) {
        if (destinatario == null) {
            return;
        }
        notificacaoRepository.save(Notificacao.builder()
                .usuario(destinatario)
                .chamado(chamado)
                .mensagem(mensagem)
                .lida(false)
                .build());
    }

    /** Chamado novo entra na fila: todos os tecnicos ficam sabendo. */
    @Transactional
    public void notificarTecnicos(Chamado chamado, String mensagem) {
        List<Usuario> tecnicos = usuarioRepository.findByPerfil(PerfilUsuario.TECNICO);
        for (Usuario tecnico : tecnicos) {
            notificar(tecnico, chamado, mensagem);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarPorUsuario(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId).stream()
                .map(NotificacaoResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificacao", id));
        notificacao.setLida(true);
        return NotificacaoResponseDTO.de(notificacaoRepository.save(notificacao));
    }
}
