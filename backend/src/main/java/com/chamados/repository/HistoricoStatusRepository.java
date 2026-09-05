package com.chamados.repository;

import com.chamados.domain.entity.HistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findByChamadoIdOrderByDataAlteracaoAsc(Long chamadoId);
}
