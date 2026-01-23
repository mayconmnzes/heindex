package br.com.heimdex.dto;

import br.com.heimdex.model.enums.ItemStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultadoChecklistResponseDTO {
    // ID do ItemChecklist (o template)
    private Long itemTemplateId; 
    // O status real preenchido: OK, NAO_CONFORMIDADE, etc.
    private ItemStatus status;    
    // A observação que o técnico digitou para o item
    private String observacao;
}