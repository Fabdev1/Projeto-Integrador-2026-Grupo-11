package com.chamados.repository;

import com.chamados.domain.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    /**
     * Busca os comentarios do chamado ja com o autor carregado, para evitar
     * uma consulta extra por comentario (problema N+1) na tela de detalhes.
     */
    @Query("SELECT c FROM Comentario c JOIN FETCH c.usuario WHERE c.chamado.id = :chamadoId ORDER BY c.dataCriacao ASC")
    List<Comentario> buscarPorChamado(Long chamadoId);

    long countByChamadoId(Long chamadoId);
}
