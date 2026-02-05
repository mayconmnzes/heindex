package br.com.heimdex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Integer quantidade;
    private String tipo;
    private String tipoMovimentacao; 
    private LocalDateTime dataHora;
    private LocalDateTime dataMovimentacao;
    private String observacao;

    @ManyToOne 
    private PecaReposicao peca;
    
    @ManyToOne 
    private Equipamento equipamento;
    
    // Relação opcional com OrdemServico (quando a movimentação foi gerada por uma OS)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;
    
    @ManyToOne(fetch = FetchType.EAGER) // ✅ Adicione isso aqui
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // --- GETTERS E SETTERS ---

    public Long getId() { return id; }
    
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String t) { this.tipo = t; }
    
    public String getTipoMovimentacao() { return tipoMovimentacao; }
    public void setTipoMovimentacao(String t) { this.tipoMovimentacao = t; }
    
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime d) { this.dataHora = d; }
    
    public LocalDateTime getDataMovimentacao() { return dataMovimentacao; }
    public void setDataMovimentacao(LocalDateTime d) { this.dataMovimentacao = d; }
    
    public String getObservacao() { return observacao; }
    public void setObservacao(String o) { this.observacao = o; }
    
    public PecaReposicao getPeca() { return peca; }
    public void setPeca(PecaReposicao p) { this.peca = p; }

    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { 
        this.equipamento = equipamento; 
    }

    public OrdemServico getOrdemServico() { return ordemServico; }
    public void setOrdemServico(OrdemServico ordemServico) { this.ordemServico = ordemServico; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { 
        this.usuario = usuario; 
    }
}