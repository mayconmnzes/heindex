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
    
    // --- MÉTODOS MANUAIS CORRIGIDOS (BLINDAGEM DOCKER) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setStatus(StatusOrdemServico s) { this.status = s; }
    public StatusOrdemServico getStatus() { return status; }
    public void setTipoManutencao(String t) { this.tipoManutencao = t; }
    public String getTipoManutencao() { return tipoManutencao; }
    public void setDataAgendamento(LocalDateTime d) { this.dataAgendamento = d; }
    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    
    // NOME CORRIGIDO: Deve bater com o Service
    public void setNomeEquipamento(String n) { this.nomeEquipamento = n; }
    public String getNomeEquipamento() { return nomeEquipamento; }
    
    public void setCodigoEquipamento(String c) { this.codigoEquipamento = c; }
    public String getCodigoEquipamento() { return codigoEquipamento; }
    
    public void setNomeTecnico(String n) { this.nomeTecnico = n; }
    public String getNomeTecnico() { return nomeTecnico; }
}