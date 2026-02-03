package br.com.heimdex.dto;

import java.time.LocalDateTime;

public class MovimentacaoEstoqueResponseDTO {
    private Long id;
    private Integer quantidade;
    private String tipo;
    private LocalDateTime dataHora;
    private String observacao;
    private Long pecaId;
    private String pecaNome;
    private Long equipamentoId;
    private String equipamentoNome;
    private Long usuarioId;
    private String usuarioNome;

    // --- GETTERS (Adicionados) ---
    public Long getId() { return id; }
    public Integer getQuantidade() { return quantidade; }
    public String getTipo() { return tipo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getObservacao() { return observacao; }
    public Long getPecaId() { return pecaId; }
    public String getPecaNome() { return pecaNome; }
    public Long getEquipamentoId() { return equipamentoId; }
    public String getEquipamentoNome() { return equipamentoNome; }
    public Long getUsuarioId() { return usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }

    // --- SETTERS ---
    public void setId(Long id) { this.id = id; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public void setTipo(String t) { this.tipo = t; }
    public void setDataHora(LocalDateTime d) { this.dataHora = d; }
    public void setObservacao(String o) { this.observacao = o; }
    public void setPecaId(Long id) { this.pecaId = id; }
    public void setPecaNome(String n) { this.pecaNome = n; }
    public void setEquipamentoId(Long id) { this.equipamentoId = id; }
    public void setEquipamentoNome(String n) { this.equipamentoNome = n; }
    public void setUsuarioId(Long id) { this.usuarioId = id; }
    public void setUsuarioNome(String n) { this.usuarioNome = n; }
}
