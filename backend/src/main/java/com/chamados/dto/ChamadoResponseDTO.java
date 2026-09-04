package com.chamados.dto;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.enums.PrioridadeChamado;
import com.chamados.domain.enums.StatusChamado;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ChamadoResponseDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String nomeCategoria;
    private String nomeSolicitante;
    private String nomeTecnico;
    private StatusChamado status;
    private PrioridadeChamado prioridade;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public ChamadoResponseDTO(Chamado chamado) {
        this.id = chamado.getId();
        this.titulo = chamado.getTitulo();
        this.descricao = chamado.getDescricao();
        this.nomeCategoria = chamado.getCategoria() != null ? chamado.getCategoria().getNome() : null;
        this.nomeSolicitante = chamado.getSolicitante() != null ? chamado.getSolicitante().getNome() : null;
        this.nomeTecnico = chamado.getTecnico() != null ? chamado.getTecnico().getNome() : null;
        this.status = chamado.getStatus();
        this.prioridade = chamado.getPrioridade();
        this.dataCriacao = chamado.getDataCriacao();
        this.dataAtualizacao = chamado.getDataAtualizacao();
    }
}
