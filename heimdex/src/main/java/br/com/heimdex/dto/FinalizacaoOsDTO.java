package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class FinalizacaoOsDTO {
    private String observacoesTecnico;
    // CRUCIAL: Lista de resultados do checklist
    private List<ResultadoRequestDTO> resultados; 
}