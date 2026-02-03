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
    private String nomeArea; // ✅ ADICIONADO: Para mostrar o setor na tabela de estoque

    public PecaReposicaoResponseDTO() {}

    // --- GETTERS E SETTERS MANUAIS (Blindagem para compilação e integração) ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }
    
    public String getCodigoControle() { return codigoControle; }
    public void setCodigoControle(String c) { this.codigoControle = c; }
    
    public Integer getEstoqueAtual() { return estoqueAtual; }
    public void setEstoqueAtual(Integer e) { this.estoqueAtual = e; }
    
    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(Integer e) { this.estoqueMinimo = e; }
    
    public String getLocalizacaoPrateleira() { return localizacaoPrateleira; }
    public void setLocalizacaoPrateleira(String l) { this.localizacaoPrateleira = l; }
    
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String f) { this.fotoUrl = f; }
    
    public Long getModeloEquipamentoId() { return modeloEquipamentoId; }
    public void setModeloEquipamentoId(Long id) { this.modeloEquipamentoId = id; }
    
    public String getNomeModeloEquipamento() { return nomeModeloEquipamento; }
    public void setNomeModeloEquipamento(String n) { this.nomeModeloEquipamento = n; }
    
    public String getFabricanteModeloEquipamento() { return fabricanteModeloEquipamento; }
    public void setFabricanteModeloEquipamento(String f) { this.fabricanteModeloEquipamento = f; }

    // ✅ MÉTODO ADICIONADO: Resolve o erro de compilação no Controller
    public String getNomeArea() { return nomeArea; }
    public void setNomeArea(String nomeArea) { this.nomeArea = nomeArea; }

    // ✅ SETTERS EXIGIDOS PELO RELATORIOCONTROLLER E MAPEAMENTO
    public void setCodigoRequisicao(String c) { this.codigoRequisicao = c; }
    public String getCodigoRequisicao() { return codigoRequisicao; }
    
    public void setDescricaoTecnica(String d) { this.descricaoTecnica = d; }
    public String getDescricaoTecnica() { return descricaoTecnica; }
    
    public void setAplicacao(String a) { this.aplicacao = a; }
    public String getAplicacao() { return aplicacao; }
}