package br.com.heimdex.dto;

import java.util.List;

public class ChecklistResponseDTO {
    private Long id;
    private String nome;
    private String areaNome;
    private List<ItemChecklistDTO> itens;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getAreaNome() { return areaNome; }
    public List<ItemChecklistDTO> getItens() { return itens; }

    public void setId(Long id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setAreaNome(String n) { this.areaNome = n; }
    public void setItens(List<ItemChecklistDTO> i) { this.itens = i; }
}
