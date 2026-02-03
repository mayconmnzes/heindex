package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinhaDeProducaoResponseDTO {
    private Long id;
    private String nome;
    private Long areaId;
    private String areaNome;
}