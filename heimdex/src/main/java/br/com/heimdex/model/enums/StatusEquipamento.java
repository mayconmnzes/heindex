// src/main/java/br.com.heimdex.model.enums/StatusEquipamento.java
package br.com.heimdex.model.enums;

/**
 * Define o status atual de um equipamento na fábrica.
 */
public enum StatusEquipamento {
    OPERACIONAL,
    AGUARDANDO_PECAS,
    EM_MANUTENCAO,
    FORA_DE_OPERACAO,
    AGUARDANDO_CALIBRACAO
}