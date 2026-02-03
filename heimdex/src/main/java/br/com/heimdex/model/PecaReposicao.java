package br.com.heimdex.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity
@Table(name = "pecas_reposicao")
public class PecaReposicao {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private String codigoControle;
    private Integer estoqueAtual;
    private Integer estoqueMinimo;
    private String fotoUrl;
    private String localizacaoPrateleira;
    private String codigoRequisicao;
    private String descricaoTecnica;
    private String aplicacao;

    // ✅ CAMPO AUXILIAR: Necessário para o Jackson ler o ID do JSON vindo do Front
    @Transient
    @JsonProperty("modeloEquipamentoId") // Nome exato que o seu Front envia
    private Long modeloEquipamentoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private ModeloEquipamento modeloEquipamento;

    public PecaReposicao() {}

    // --- MÉTODOS MANUAIS ---
    
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
    
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String f) { this.fotoUrl = f; }
    
    public String getLocalizacaoPrateleira() { return localizacaoPrateleira; }
    public void setLocalizacaoPrateleira(String l) { this.localizacaoPrateleira = l; }
    
    public String getCodigoRequisicao() { return codigoRequisicao; }
    public void setCodigoRequisicao(String c) { this.codigoRequisicao = c; }
    
    public String getDescricaoTecnica() { return descricaoTecnica; }
    public void setDescricaoTecnica(String d) { this.descricaoTecnica = d; }
    
    public String getAplicacao() { return aplicacao; }
    public void setAplicacao(String a) { this.aplicacao = a; }

    public ModeloEquipamento getModeloEquipamento() { return modeloEquipamento; }
    public void setModeloEquipamento(ModeloEquipamento m) { this.modeloEquipamento = m; }

    // ✅ GETTER/SETTER PARA O CAMPO AUXILIAR (Crucial para o Controller)
    public Long getModeloEquipamentoId() { return modeloEquipamentoId; }
    public void setModeloEquipamentoId(Long id) { this.modeloEquipamentoId = id; }
}