package com.chamados.dto;

import java.util.List;

/**
 * Payload da tela de detalhes: chamado, rastro de status, conversa e avaliacao
 * em uma unica requisicao. Evita quatro chamadas separadas ao abrir o modal.
 */
public record ChamadoDetalheResponseDTO(
        ChamadoResponseDTO chamado,
        List<HistoricoStatusResponseDTO> historico,
        List<ComentarioResponseDTO> comentarios,
        AvaliacaoResponseDTO avaliacao
) {}
