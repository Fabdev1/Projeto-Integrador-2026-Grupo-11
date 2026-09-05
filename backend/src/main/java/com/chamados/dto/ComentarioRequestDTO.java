package com.chamados.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComentarioRequestDTO(

        @NotNull(message = "Informe quem esta comentando")
        Long usuarioId,

        @NotBlank(message = "A mensagem nao pode ficar vazia")
        @Size(max = 2000, message = "A mensagem passa de 2000 caracteres")
        String mensagem
) {}
