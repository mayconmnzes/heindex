package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List; // NOVO IMPORT

@Getter
@Setter
public class ItemChecklistDTO {
    private Long id;
    private String descricao;
    
    // NOVO: Lista de peças sugeridas para este item
    private List<PecaReposicaoResponseDTO> pecasSugeridas; 
}