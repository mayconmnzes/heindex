package br.com.heimdex.dto;

public class MovimentacaoEstoqueDTO {
    private Long pecaId;
    private Integer quantidade;
    private String tipo; // "ENTRADA" ou "SAIDA"

    public Long getPecaId() { return pecaId; }
    public void setPecaId(Long id) { this.pecaId = id; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer q) { this.quantidade = q; }
    public String getTipo() { return tipo; }
    public void setTipo(String t) { this.tipo = t; }
}