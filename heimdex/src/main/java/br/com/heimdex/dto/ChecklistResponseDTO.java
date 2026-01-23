package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ChecklistResponseDTO {
    private Long id;
    private String nome;
    private String areaNome; // NOVO: Nome da área para exibição
    private List<ItemChecklistDTO> itens;
}