package com.chamados.dto;

import com.chamados.domain.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A coluna historico_status.alterado_por e NOT NULL no DDL. Por isso a troca de
 * status passou a exigir o autor da mudanca no corpo da requisicao, e nao mais
 * apenas o novo status na query string.
 */
public record AlterarStatusRequestDTO(

        @NotNull(message = "Informe o novo status")
        StatusChamado statusNovo,

        @NotNull(message = "Informe quem esta alterando o status")
        Long usuarioId,

        @Size(max = 2000, message = "O comentario passa de 2000 caracteres")
        String comentario
) {}
