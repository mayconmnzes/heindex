package br.com.heimdex.dto;

import br.com.heimdex.model.enums.StatusOrdemServico;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Collections;
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
    private List<ResultadoChecklistResponseDTO> resultados = Collections.emptyList();
    private List<String> fotosEvidencia = Collections.emptyList();
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
    private LocalDateTime dataValidacao;
    private String observacoesLider;
    private String nomeLider;
    private List<PecaConsumidaOsDTO> pecasConsumidas = Collections.emptyList();

    public OrdemServicoResponseDTO() {}
    
    // Getters manuais para blindagem contra falha do Lombok
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setStatus(StatusOrdemServico s) { this.status = s; }
    public void setEquipamentoNome(String n) { this.nomeEquipamento = n; }
    public void setDataAgendamento(LocalDateTime d) { this.dataAgendamento = d; }
}