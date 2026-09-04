package com.chamados.repository;

import com.chamados.domain.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByChamadoId(Long chamadoId);

    boolean existsByChamadoId(Long chamadoId);
}
