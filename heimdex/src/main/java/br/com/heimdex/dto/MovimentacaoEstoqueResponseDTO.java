package br.com.heimdex.dto;

import java.time.LocalDateTime;

public class MovimentacaoEstoqueResponseDTO {
    private Long id;
    private Long pecaId;
    private Integer quantidade;
    private String tipoMovimentacao;
    private LocalDateTime dataMovimentacao;
    private String nomePeca;
    private String nomeEquipamento;
    private String loginUsuario;
    private String observacao;

    public MovimentacaoEstoqueResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPecaId() { return pecaId; }
    public void setPecaId(Long pecaId) { this.pecaId = pecaId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getTipoMovimentacao() { return tipoMovimentacao; }
    public void setTipoMovimentacao(String tipoMovimentacao) { this.tipoMovimentacao = tipoMovimentacao; }

    public LocalDateTime getDataMovimentacao() { return dataMovimentacao; }
    public void setDataMovimentacao(LocalDateTime dataMovimentacao) { this.dataMovimentacao = dataMovimentacao; }

    public String getNomePeca() { return nomePeca; }
    public void setNomePeca(String nomePeca) { this.nomePeca = nomePeca; }

    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String nomeEquipamento) { this.nomeEquipamento = nomeEquipamento; }

    public String getLoginUsuario() { return loginUsuario; }
    public void setLoginUsuario(String loginUsuario) { this.loginUsuario = loginUsuario; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}