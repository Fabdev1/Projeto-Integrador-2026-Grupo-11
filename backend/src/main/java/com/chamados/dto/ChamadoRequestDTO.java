package com.chamados.dto;

import com.chamados.domain.enums.PrioridadeChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChamadoRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200, message = "O título não pode passar de 200 caracteres")
    private String titulo;

    private String descricao;

    @NotNull(message = "A categoria é obrigatória")
    private Long categoriaId;

    @NotNull(message = "O solicitante é obrigatório")
    private Long solicitanteId;

    private PrioridadeChamado prioridade;
}
