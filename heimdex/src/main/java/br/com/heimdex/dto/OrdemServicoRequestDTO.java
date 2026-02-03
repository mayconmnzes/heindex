package br.com.heimdex.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record OrdemServicoRequestDTO(
    Long equipamentoId,
    Set<Long> tecnicosIds,
    Long checklistId,
    String tipoManutencao,
    LocalDateTime dataAgendamento
) {}