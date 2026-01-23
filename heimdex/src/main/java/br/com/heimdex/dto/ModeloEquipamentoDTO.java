// Código Completo
package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO para criar e exibir Modelos de Equipamento (Tipos).
 */
@Getter
@Setter
public class ModeloEquipamentoDTO {

    private Long id;
    private String nome; // Ex: "SM471"
    private String fabricante; // Ex: "Samsung"

    // ID e Nome da Área à qual o modelo pertence
    private Long areaId;
    private String areaNome;

    // Contagem de quantas instâncias (equipamentos/tags) existem
    private int quantidadeInstancias;
    
    // Contagem de quantas peças estão associadas
    private int quantidadePecasAssociadas;
}