package br.com.heimdex.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor // Facilita a criação do objeto
public class CurvaAbcDTO {
    
    private Long pecaId;
    private String nomePeca;
    private String codigoControle;
    private Long totalConsumido;
    private double percentualTotal;
    private double percentualAcumulado;
    private String classificacao; // "A", "B", ou "C"

}