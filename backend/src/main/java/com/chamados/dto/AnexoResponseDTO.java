package com.chamados.dto;

import com.chamados.domain.entity.Anexo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnexoResponseDTO {

    private Long id;
    private Long chamadoId;
    private String nomeEnviadoPor;
    private String nomeArquivo;
    private String urlDownload;
    private LocalDateTime dataEnvio;

    public AnexoResponseDTO(Anexo anexo) {
        this.id = anexo.getId();
        this.chamadoId = anexo.getChamado().getId();
        this.nomeEnviadoPor = anexo.getEnviadoPor().getNome();
        this.nomeArquivo = anexo.getNomeArquivo();
        this.urlDownload = "/api/anexos/" + anexo.getId() + "/download";
        this.dataEnvio = anexo.getDataEnvio();
    }
}
