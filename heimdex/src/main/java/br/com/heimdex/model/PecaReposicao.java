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
    private String codigoControle;
    private Integer estoqueAtual = 0;
    private Integer estoqueMinimo = 0;
    private String fotoUrl;
    private String localizacaoPrateleira;
    private String codigoRequisicao;
    private String descricaoTecnica;
    private String aplicacao;

    // ✅ CAPTURA IDs DO FRONT: Necessário para o Jackson converter o JSON
    @Transient
    @JsonProperty("modelosIds") 
    private List<Long> modelosIds;

    // ✅ RELAÇÃO PARA MÚLTIPLAS MÁQUINAS: Cria a tabela 'pecas_modelos' no H2
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "pecas_modelos",
        joinColumns = @JoinColumn(name = "peca_id"),
        inverseJoinColumns = @JoinColumn(name = "modelo_id")
    )
    private List<ModeloEquipamento> modelosEquipamentos = new ArrayList<>();

    public PecaReposicao() {}

    // --- MÉTODOS DE ACESSO ---
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

    public List<ModeloEquipamento> getModelosEquipamentos() { return modelosEquipamentos; }
    public void setModelosEquipamentos(List<ModeloEquipamento> m) { this.modelosEquipamentos = m; }

    public List<Long> getModelosIds() { return modelosIds; }
    public void setModelosIds(List<Long> ids) { this.modelosIds = ids; }

    // ✅ MÉTODO SOLICITADO PELO SERVICE: Resolve o erro de compilação
    public void setModeloEquipamento(ModeloEquipamento m) {
        if (this.modelosEquipamentos == null) this.modelosEquipamentos = new ArrayList<>();
        if (m != null && !this.modelosEquipamentos.contains(m)) {
            this.modelosEquipamentos.add(m);
        }
    }

    // ✅ MÉTODO DE COMPATIBILIDADE: Para o RelatorioController não quebrar
    public ModeloEquipamento getModeloEquipamento() { 
        return (modelosEquipamentos != null && !modelosEquipamentos.isEmpty()) ? modelosEquipamentos.get(0) : null; 
    }

    public Long getModeloEquipamentoId() { 
        return (modelosIds != null && !modelosIds.isEmpty()) ? modelosIds.get(0) : null; 
    }
}