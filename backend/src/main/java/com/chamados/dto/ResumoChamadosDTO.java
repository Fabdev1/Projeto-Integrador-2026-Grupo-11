package com.chamados.dto;

/**
 * Contadores do painel. Calculados no banco para que a tela inicial nao precise
 * baixar a lista inteira de chamados so para contar.
 */
public record ResumoChamadosDTO(
        long abertos,
        long emAndamento,
        long concluidos,
        long total
) {}
