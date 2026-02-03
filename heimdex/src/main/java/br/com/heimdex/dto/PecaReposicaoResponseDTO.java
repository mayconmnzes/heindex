package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PecaReposicaoResponseDTO {
    private Long id;
    private String codigoControle;
    private String nome;
    private String codigoRequisicao;
    private String descricaoTecnica;
    private String aplicacao;
    private String localizacaoPrateleira;
    private Integer estoqueAtual;
    private Integer estoqueMinimo;
    private String fotoUrl;
    private Long modeloEquipamentoId;
    private String nomeModeloEquipamento;
    private String fabricanteModeloEquipamento;

    public PecaReposicaoResponseDTO() {}

    // Getters/Setters manuais para blindagem completa
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }
    public String getCodigoControle() { return codigoControle; }
    public void setCodigoControle(String c) { this.codigoControle = c; }
    public Integer getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(Integer e) { this.estoqueAtual = e; }
    public void setEstoqueMinimo(Integer e) { this.estoqueMinimo = e; }
    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setLocalizacaoPrateleira(String l) { this.localizacaoPrateleira = l; }
    public String getLocalizacaoPrateleira() { return localizacaoPrateleira; }
    public void setFotoUrl(String f) { this.fotoUrl = f; }
    public String getFotoUrl() { return fotoUrl; }
    public void setModeloEquipamentoId(Long id) { this.modeloEquipamentoId = id; }
    public Long getModeloEquipamentoId() { return modeloEquipamentoId; }
    public void setNomeModeloEquipamento(String n) { this.nomeModeloEquipamento = n; }
    public String getNomeModeloEquipamento() { return nomeModeloEquipamento; }
    public void setFabricanteModeloEquipamento(String f) { this.fabricanteModeloEquipamento = f; }
    public String getFabricanteModeloEquipamento() { return fabricanteModeloEquipamento; }
}