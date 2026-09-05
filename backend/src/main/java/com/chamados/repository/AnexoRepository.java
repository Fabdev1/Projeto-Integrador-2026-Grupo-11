package com.chamados.repository;

import com.chamados.domain.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByChamadoIdOrderByDataEnvioAsc(Long chamadoId);
}
