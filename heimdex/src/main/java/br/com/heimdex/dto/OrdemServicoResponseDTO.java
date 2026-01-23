// Código Completo Modificado - OrdemServicoResponseDTO.java
package br.com.heimdex.dto;

import br.com.heimdex.model.enums.StatusOrdemServico;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Collections; // Import para lista vazia
import java.util.List;

@Getter
@Setter
public class OrdemServicoResponseDTO {
    private Long id;
    private StatusOrdemServico status;
    private String tipoManutencao;
    private LocalDateTime dataAgendamento;
    private String observacoesTecnico;

    private String nomeEquipamento;
    private String codigoEquipamento;
    private String nomeTecnico;

    private ChecklistResponseDTO checklist;
    private List<ResultadoChecklistResponseDTO> resultados = Collections.emptyList(); // Inicializa
    private List<String> fotosEvidencia = Collections.emptyList(); // Inicializa
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
    private LocalDateTime dataValidacao;
    private String observacoesLider;
    private String nomeLider;

    // --- NOVA ALTERAÇÃO ---
    // Adiciona a lista de peças consumidas nesta OS
    private List<PecaConsumidaOsDTO> pecasConsumidas = Collections.emptyList(); // Inicializa
    // --- FIM DA ALTERAÇÃO ---
}