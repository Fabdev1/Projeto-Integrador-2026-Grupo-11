package com.chamados.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anexos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por", nullable = false)
    private Usuario enviadoPor;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "caminho_arquivo", length = 500)
    private String caminhoArquivo;

    @Column(name = "data_envio", updatable = false)
    private LocalDateTime dataEnvio;

    @PrePersist
    public void prePersist() {
        if (this.dataEnvio == null) {
            this.dataEnvio = LocalDateTime.now();
        }
    }
}
