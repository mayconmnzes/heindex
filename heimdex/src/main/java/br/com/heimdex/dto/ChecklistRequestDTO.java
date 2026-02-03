package br.com.heimdex.dto;

import java.util.List;
import java.util.Set;

public class ChecklistRequestDTO {
    private String nome;
    private Long areaId;
    private List<ItemChecklistRequestDTO> itens;

    public static class ItemChecklistRequestDTO {
        private String descricao;
        private Set<Long> pecasSugeridasIds; // ✅ Adicionado para matar o erro

        public String getDescricao() { return descricao; }
        public void setDescricao(String d) { this.descricao = d; }
        public Set<Long> getPecasSugeridasIds() { return pecasSugeridasIds; }
        public void setPecasSugeridasIds(Set<Long> ids) { this.pecasSugeridasIds = ids; }
    }

    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long id) { this.areaId = id; }
    public List<ItemChecklistRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemChecklistRequestDTO> i) { this.itens = i; }
}