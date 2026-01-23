package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovimentacaoEstoqueDTO {
    private int quantidade;
    
    // CAMPO CRÍTICO FALTANTE: ID da peça que está sendo movimentada
    private Long pecaId; 
}