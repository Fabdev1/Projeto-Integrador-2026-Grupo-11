package com.chamados.repository;

import com.chamados.domain.entity.Chamado;
import com.chamados.domain.enums.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findBySolicitanteId(Long solicitanteId);

    List<Chamado> findByTecnicoId(Long tecnicoId);

    List<Chamado> findByStatus(StatusChamado status);
}
