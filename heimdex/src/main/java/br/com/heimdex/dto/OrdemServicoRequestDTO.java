package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class OrdemServicoRequestDTO {
    private Long equipamentoId;
    private Long tecnicoId;
    private Long checklistId;
    private LocalDateTime dataAgendamento;
    private String tipoManutencao; // <-- NOVO CAMPO
}