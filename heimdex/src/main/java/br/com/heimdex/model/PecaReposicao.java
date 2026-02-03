package br.com.heimdex.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "pecas_reposicao")
public class PecaReposicao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private ModeloEquipamento modeloEquipamento;

    public PecaReposicao() {}

    // --- MÉTODOS MANUAIS (BLINDAGEM CONTRA FALHA DO LOMBOK) ---
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
    public String getCodigoRequisicao() { return codigoRequisicao; }
    public String getDescricaoTecnica() { return descricaoTecnica; }
    public String getAplicacao() { return aplicacao; }
    public ModeloEquipamento getModeloEquipamento() { return modeloEquipamento; }
}