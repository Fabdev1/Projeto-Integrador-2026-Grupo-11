package com.chamados.repository;

import com.chamados.domain.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * O upload de arquivos ficou fora do recorte da prova de conceito
 * (ver docs/01-prova-de-conceito.md, secao 2.1). O repositorio existe para
 * completar o mapeamento das 8 tabelas do modelo da primeira etapa.
 */
@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByChamadoId(Long chamadoId);
}
