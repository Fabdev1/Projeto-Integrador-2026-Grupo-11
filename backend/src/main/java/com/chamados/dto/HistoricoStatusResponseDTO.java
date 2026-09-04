package com.chamados.dto;

import com.chamados.domain.entity.HistoricoStatus;
import com.chamados.domain.enums.StatusChamado;

import java.time.LocalDateTime;

public record HistoricoStatusResponseDTO(
        Long id,
        StatusChamado statusAnterior,
        StatusChamado statusNovo,
        Long alteradoPorId,
        String alteradoPorNome,
        LocalDateTime dataAlteracao
) {
    public static HistoricoStatusResponseDTO de(HistoricoStatus historico) {
        return new HistoricoStatusResponseDTO(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getAlteradoPor().getId(),
                historico.getAlteradoPor().getNome(),
                historico.getDataAlteracao());
    }
}
