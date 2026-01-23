// Código Completo
package br.com.heimdex.dto;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EquipamentoRequestDTO {

    // --- ALTERAÇÃO ---
    // 'nome' agora é a "Tag". Ex: "SM471A"
    private String nome;
    private String codigo;

    // --- REMOVIDO ---
    // 'fabricante' foi movido para ModeloEquipamento
    // private String fabricante;

    private CriticidadeEquipamento criticidade;

    // --- ALTERAÇÃO ---
    // Agora precisamos do ID do Modelo (Tipo) ao qual este equipamento (Tag) pertence
    private Long modeloId;

    // LinhaId permanece, pois a Tag/Instância está fisicamente na linha
    private Long linhaId;
    private Long checklistId;

    // Campos de Preventiva (Sem alteração)
    private FrequenciaPreventiva frequenciaPreventiva;
    private LocalDate dataUltimaPreventiva;
}