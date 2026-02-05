package br.com.heimdex.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pecas_reposicao")
public class PecaReposicao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(name = "codigo_controle")
    private String codigoControle;

    @Column(name = "estoque_atual")
    private Integer estoqueAtual = 0;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo = 0;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "localizacao_prateleira")
    private String localizacaoPrateleira;

    @Column(name = "codigo_requisicao")
    private String codigoRequisicao;

    @Column(name = "descricao_tecnica")
    private String descricaoTecnica;

    private String aplicacao;

    // IDs vindos do front (transiente)
    @Transient
    @JsonProperty("modelosIds")
    private List<Long> modelosIds;

    @Column(name = "qr_url")
    private String qrUrl;
    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "pecas_modelos",
        joinColumns = @JoinColumn(name = "peca_id"),
        inverseJoinColumns = @JoinColumn(name = "modelo_id")
    )
    private List<ModeloEquipamento> modelosEquipamentos = new ArrayList<>();

    public PecaReposicao() {}

    // --- getters / setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }

    // JSON mapping: aceita "codigoControle" e "codigo_controle"
    @JsonProperty("codigoControle")
    public String getCodigoControle() { return codigoControle; }

    @JsonProperty("codigoControle")
    public void setCodigoControle(String codigoControle) { this.codigoControle = codigoControle; }

    // também aceita underscored name no JSON
    @JsonProperty("codigo_controle")
    public void setCodigoControleUnderscore(String codigo) { this.codigoControle = codigo; }

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

    public List<ModeloEquipamento> getModelosEquipamentos() { return modelosEquipamentos; }
    public void setModelosEquipamentos(List<ModeloEquipamento> m) { this.modelosEquipamentos = m; }

    public List<Long> getModelosIds() { return modelosIds; }
    public void setModelosIds(List<Long> ids) { this.modelosIds = ids; }

    // compatibilidade com service
    public void setModeloEquipamento(ModeloEquipamento m) {
        if (this.modelosEquipamentos == null) this.modelosEquipamentos = new ArrayList<>();
        if (m != null && !this.modelosEquipamentos.contains(m)) {
            this.modelosEquipamentos.add(m);
        }
    }

    public ModeloEquipamento getModeloEquipamento() {
        return (modelosEquipamentos != null && !modelosEquipamentos.isEmpty()) ? modelosEquipamentos.get(0) : null;
    }

    public Long getModeloEquipamentoId() {
        return (modelosIds != null && !modelosIds.isEmpty()) ? modelosIds.get(0) : null;
    }

    @Override
    public String toString() {
        return "PecaReposicao{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", codigoControle='" + codigoControle + '\'' +
                ", codigoRequisicao='" + codigoRequisicao + '\'' +
                ", estoqueAtual=" + estoqueAtual +
                ", estoqueMinimo=" + estoqueMinimo +
                '}';
    }
}