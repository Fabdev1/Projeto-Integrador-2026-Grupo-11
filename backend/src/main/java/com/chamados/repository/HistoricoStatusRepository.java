package com.chamados.repository;

import com.chamados.domain.entity.HistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    @Query("SELECT h FROM HistoricoStatus h JOIN FETCH h.alteradoPor WHERE h.chamado.id = :chamadoId ORDER BY h.dataAlteracao ASC, h.id ASC")
    List<HistoricoStatus> buscarPorChamado(Long chamadoId);
}
