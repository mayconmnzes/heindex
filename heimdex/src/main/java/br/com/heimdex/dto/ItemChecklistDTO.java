package br.com.heimdex.dto;

import java.util.List;

public class ItemChecklistDTO {
    private Long id;
    private String descricao;
    private List<PecaReposicaoResponseDTO> pecasSugeridas;

    public ItemChecklistDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public List<PecaReposicaoResponseDTO> getPecasSugeridas() { return pecasSugeridas; }
    public void setPecasSugeridas(List<PecaReposicaoResponseDTO> pecasSugeridas) { this.pecasSugeridas = pecasSugeridas; }
}
