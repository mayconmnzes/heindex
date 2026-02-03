// Código Completo - PecaConsumidaOsDTO.java
package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * DTO para representar uma peça consumida dentro do histórico de uma Ordem de Serviço.
 */
@Getter
@Setter
public class PecaConsumidaOsDTO {
    private Long pecaId;
    private String nomePeca;
    private String codigoControle;
    private int quantidadeBaixada;
    private LocalDateTime dataBaixa;
}