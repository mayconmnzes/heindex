package br.com.heimdex.dto;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import java.time.LocalDate;

public class EquipamentoResponseDTO {
    private Long id;
    private String nome;
    private String codigo;
    private CriticidadeEquipamento criticidade;
    private Long modeloId;
    private String nomeModelo;
    private String fabricante;
    private FrequenciaPreventiva frequenciaPreventiva;
    private LocalDate dataUltimaPreventiva;
    private String statusPreventiva;
    private String nomeLinha;
    private String nomeArea;
    private String checklistNome;
    private Long checklistId;

    // --- GETTERS (Essenciais para o site ver os dados) ---
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public CriticidadeEquipamento getCriticidade() { return criticidade; }
    public Long getModeloId() { return modeloId; }
    public String getNomeModelo() { return nomeModelo; }
    public String getFabricante() { return fabricante; }
    public FrequenciaPreventiva getFrequenciaPreventiva() { return frequenciaPreventiva; }
    public LocalDate getDataUltimaPreventiva() { return dataUltimaPreventiva; }
    public String getStatusPreventiva() { return statusPreventiva; }
    public String getNomeLinha() { return nomeLinha; }
    public String getNomeArea() { return nomeArea; }
    public String getChecklistNome() { return checklistNome; }
    public Long getChecklistId() { return checklistId; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setCriticidade(CriticidadeEquipamento c) { this.criticidade = c; }
    public void setModeloId(Long id) { this.modeloId = id; }
    public void setNomeModelo(String n) { this.nomeModelo = n; }
    public void setFabricante(String f) { this.fabricante = f; }
    public void setFrequenciaPreventiva(FrequenciaPreventiva f) { this.frequenciaPreventiva = f; }
    public void setDataUltimaPreventiva(LocalDate d) { this.dataUltimaPreventiva = d; }
    public void setStatusPreventiva(String s) { this.statusPreventiva = s; }
    public void setNomeLinha(String n) { this.nomeLinha = n; }
    public void setNomeArea(String n) { this.nomeArea = n; }
    public void setChecklistNome(String n) { this.checklistNome = n; }
    public void setChecklistId(Long id) { this.checklistId = id; }
}
