package com.chamados.repository;

import com.chamados.domain.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByChamadoIdOrderByDataCriacaoAsc(Long chamadoId);
}
