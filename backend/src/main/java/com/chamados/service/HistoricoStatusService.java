package com.chamados.service;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.entity.HistoricoStatus;
import com.chamados.domain.entity.Usuario;
import com.chamados.domain.enums.StatusChamado;
import com.chamados.dto.HistoricoStatusResponseDTO;
import com.chamados.repository.HistoricoStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Auditoria do ciclo de vida do chamado.
 *
 * Na primeira etapa a tabela historico_status foi modelada mas nada a
 * alimentava: a troca de status gravava apenas o novo valor em chamados. Cada
 * transicao agora passa por aqui, dentro da mesma transacao da alteracao.
 */
@Service
public class HistoricoStatusService {

    private final HistoricoStatusRepository historicoStatusRepository;

    public HistoricoStatusService(HistoricoStatusRepository historicoStatusRepository) {
        this.historicoStatusRepository = historicoStatusRepository;
    }

    @Transactional
    public HistoricoStatus registrar(Chamado chamado,
                                     Usuario autor,
                                     StatusChamado statusAnterior,
                                     StatusChamado statusNovo) {
        HistoricoStatus evento = HistoricoStatus.builder()
                .chamado(chamado)
                .alteradoPor(autor)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .build();
        return historicoStatusRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResponseDTO> listarPorChamado(Long chamadoId) {
        return historicoStatusRepository.buscarPorChamado(chamadoId).stream()
                .map(HistoricoStatusResponseDTO::de)
                .toList();
    }
}
