package com.chamados.dto;

import com.chamados.domain.enums.PrioridadeChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    private String titulo;

    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    private Long categoriaId;

    @NotNull(message = "O solicitante é obrigatório")
    private Long solicitanteId;

    private PrioridadeChamado prioridade;
}
