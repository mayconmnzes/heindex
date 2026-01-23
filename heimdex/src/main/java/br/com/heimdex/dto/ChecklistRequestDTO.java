package br.com.heimdex.dto;

import lombok.Getter; 
import lombok.Setter; 
import java.util.List; 

@Getter
@Setter
public class ChecklistRequestDTO {
    private String nome;
    private Long areaId;
    
    // NOVO: A lista agora é de objetos, não mais de strings
    private List<ItemChecklistRequestDTO> itens; 
    
    // Nova classe interna para representar o item na requisição
    @Getter
    @Setter
    public static class ItemChecklistRequestDTO {
        private String descricao;
        private List<Long> pecasSugeridasIds;
    }
}