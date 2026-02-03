package br.com.heimdex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantidade;
    private String tipo;
    private String tipoMovimentacao; 
    private LocalDateTime dataHora;
    private LocalDateTime dataMovimentacao;
    private String observacao;

    @ManyToOne private PecaReposicao peca;
    @ManyToOne private Equipamento equipamento;
    @ManyToOne private Usuario usuario;

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
    public Usuario getUsuario() { return usuario; }
}