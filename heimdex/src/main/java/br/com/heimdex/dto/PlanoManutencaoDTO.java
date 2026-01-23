package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanoManutencaoDTO {
    private Long id;
    private String nome;
    private Long equipamentoId;
    private String nomeEquipamento;
    private String codigoEquipamento;
    private Integer periodicidadeDias;
    private String proximaExecucao; // String no formato ISO (YYYY-MM-DD)
    private boolean ativo;
}