package com.chamados.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvaliacaoRequestDTO {

    @NotNull(message = "O chamado é obrigatório")
    private Long chamadoId;

    @NotNull(message = "O usuário é obrigatório")
    private Long usuarioId;

    @NotNull(message = "A nota é obrigatória")
    @Min(value = 1, message = "A nota mínima é 1")
    @Max(value = 5, message = "A nota máxima é 5")
    private Integer nota;

    private String comentario;
}
