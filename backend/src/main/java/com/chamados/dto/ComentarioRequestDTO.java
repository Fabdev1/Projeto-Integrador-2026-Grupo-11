package com.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioRequestDTO {

    @NotNull(message = "O chamado é obrigatório")
    private Long chamadoId;

    @NotNull(message = "O usuário é obrigatório")
    private Long usuarioId;

    @NotBlank(message = "A mensagem é obrigatória")
    private String mensagem;
}
