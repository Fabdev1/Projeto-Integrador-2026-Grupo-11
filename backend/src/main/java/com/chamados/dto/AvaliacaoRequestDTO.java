package com.chamados.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliacaoRequestDTO(

        @NotNull(message = "Informe quem esta avaliando")
        Long usuarioId,

        @NotNull(message = "A nota e obrigatoria")
        @Min(value = 1, message = "A nota vai de 1 a 5")
        @Max(value = 5, message = "A nota vai de 1 a 5")
        Integer nota,

        @Size(max = 1000, message = "O comentario passa de 1000 caracteres")
        String comentario
) {}
