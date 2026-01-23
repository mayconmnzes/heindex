// Código Completo
package br.com.heimdex.dto;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EquipamentoResponseDTO {
    private Long id;

    // --- ALTERAÇÃO ---
    // 'nome' é a Tag (Ex: "SM471A")
    private String nome;
    private String codigo;

    // --- ALTERAÇÃO ---
    // Adicionamos os detalhes do Modelo (Tipo)
    private Long modeloId;
    private String nomeModelo; // Ex: "SM471"
    private String fabricante; // Ex: "Samsung" (Vem do modelo)
    
    // --- SEM ALTERAÇÃO ---
    private CriticidadeEquipamento criticidade;
    private String nomeLinha;
    private String nomeArea;
    private String checklistNome;
    private Long checklistId;

    // Campos de Preventiva (Sem alteração)
    private FrequenciaPreventiva frequenciaPreventiva;
    private LocalDate dataUltimaPreventiva;
    private String statusPreventiva;
}