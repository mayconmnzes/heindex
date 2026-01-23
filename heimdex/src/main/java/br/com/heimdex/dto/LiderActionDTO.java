package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime; // NOVO IMPORT

@Getter
@Setter
public class LiderActionDTO {
    private Long liderId;
    private String observacoesLider;
    
    // NOVO: Para reagendamento de OS reprovada/pendente
    private LocalDateTime novaDataAgendamento; 
}