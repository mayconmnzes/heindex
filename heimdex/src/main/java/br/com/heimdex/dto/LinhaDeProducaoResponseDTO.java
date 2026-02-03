package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinhaDeProducaoResponseDTO {
    private Long id;
    private String nome;
    private Long areaId;
    private String areaNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long id) { this.areaId = id; }
    public String getAreaNome() { return areaNome; }
    public void setAreaNome(String n) { this.areaNome = n; }
}