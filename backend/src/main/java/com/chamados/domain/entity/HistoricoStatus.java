package com.chamados.domain.entity;

import com.chamados.domain.enums.StatusChamado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alterado_por", nullable = false)
    private Usuario alteradoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 50)
    private StatusChamado statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 50)
    private StatusChamado statusNovo;

    @Column(name = "data_alteracao", updatable = false)
    private LocalDateTime dataAlteracao;

    @PrePersist
    public void prePersist() {
        if (this.dataAlteracao == null) {
            this.dataAlteracao = LocalDateTime.now();
        }
    }
}
