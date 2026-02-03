package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CurvaAbcDTO {
    private Long pecaId;
    private String nomePeca;
    private String codigoControle;
    private long totalConsumido;
    private double valorUnitario;
    private double valorTotal;
    private String classificacao;

    // Construtor Manual para garantir que o Controller consiga criar o objeto
    public CurvaAbcDTO(Long pecaId, String nomePeca, String codigoControle, 
                       long totalConsumido, double valorUnitario, 
                       double valorTotal, String classificacao) {
        this.pecaId = pecaId;
        this.nomePeca = nomePeca;
        this.codigoControle = codigoControle;
        this.totalConsumido = totalConsumido;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.classificacao = classificacao;
    }
}