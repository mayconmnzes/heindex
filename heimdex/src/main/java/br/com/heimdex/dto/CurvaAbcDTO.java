package br.com.heimdex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurvaAbcDTO {
    private Long pecaId;
    private String nomePeca;
    private String codigoControle;
    private Long totalConsumido;
    private double percentualTotal;
    private double percentualAcumulado;
    private String classe; // Alterado de 'classificacao' para 'classe' para bater com o Controller
}