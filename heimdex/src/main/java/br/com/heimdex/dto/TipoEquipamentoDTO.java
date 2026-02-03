package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoEquipamentoDTO {
    private Long id;
    private String nome;
    private String fabricante;
    private Long areaId;
    private String areaNome;
}