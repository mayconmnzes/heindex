package br.com.heimdex.dto;

import java.time.LocalDate;

public class PlanoManutencaoDTO {
    private Long id;
    private String nome;
    private Integer periodicidadeDias;
    private boolean ativo;
    private LocalDate proximaExecucao;
    private Long equipamentoId;
    private String nomeEquipamento;
    private String codigoEquipamento;

    public PlanoManutencaoDTO() {}

    // --- MÉTODOS MANUAIS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getPeriodicidadeDias() { return periodicidadeDias; }
    public void setPeriodicidadeDias(Integer periodicidadeDias) { this.periodicidadeDias = periodicidadeDias; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDate getProximaExecucao() { return proximaExecucao; }
    public void setProximaExecucao(LocalDate proximaExecucao) { this.proximaExecucao = proximaExecucao; }

    public Long getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(Long equipamentoId) { this.equipamentoId = equipamentoId; }

    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String nomeEquipamento) { this.nomeEquipamento = nomeEquipamento; }

    public String getCodigoEquipamento() { return codigoEquipamento; }
    public void setCodigoEquipamento(String codigoEquipamento) { this.codigoEquipamento = codigoEquipamento; }
}
