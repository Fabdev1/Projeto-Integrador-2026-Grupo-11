package com.chamados.dto;

import com.chamados.domain.entity.HistoricoStatus;
import com.chamados.domain.enums.StatusChamado;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HistoricoStatusResponseDTO {

    private Long id;
    private StatusChamado statusAnterior;
    private StatusChamado statusNovo;
    private String nomeAlteradoPor;
    private LocalDateTime dataAlteracao;

    public HistoricoStatusResponseDTO(HistoricoStatus historico) {
        this.id = historico.getId();
        this.statusAnterior = historico.getStatusAnterior();
        this.statusNovo = historico.getStatusNovo();
        this.nomeAlteradoPor = historico.getAlteradoPor().getNome();
        this.dataAlteracao = historico.getDataAlteracao();
    }
}
