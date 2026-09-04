package com.chamados.repository;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findBySolicitanteId(Long solicitanteId);

    List<Chamado> findByTecnicoId(Long tecnicoId);

    List<Chamado> findByStatus(StatusChamado status);

    // ------------------------------------------------------------------
    // Adicoes da 2a etapa
    // ------------------------------------------------------------------

    /** Contadores do painel, calculados no banco. */
    long countByStatus(StatusChamado status);

    long countByStatusIn(Collection<StatusChamado> status);

    /**
     * Categoria, solicitante e tecnico sao LAZY. Sem o fetch join, montar a
     * lista de resposta dispara tres consultas por chamado (problema N+1).
     */
    @Query("""
            SELECT c FROM Chamado c
            LEFT JOIN FETCH c.categoria
            LEFT JOIN FETCH c.solicitante
            LEFT JOIN FETCH c.tecnico
            ORDER BY c.dataCriacao DESC, c.id DESC
            """)
    List<Chamado> buscarTodosComRelacionamentos();

    @Query("""
            SELECT c FROM Chamado c
            LEFT JOIN FETCH c.categoria
            LEFT JOIN FETCH c.solicitante
            LEFT JOIN FETCH c.tecnico
            WHERE c.id = :id
            """)
    Optional<Chamado> buscarPorIdComRelacionamentos(Long id);
}
