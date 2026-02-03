package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeloEquipamentoDTO {
    private Long id;
    private String nome;
    private String fabricante;
    private Long areaId;
    private String areaNome;
    private int quantidadeInstancias;
    private int quantidadePecasAssociadas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String n) { this.nome = n; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String f) { this.fabricante = f; }
    public void setAreaId(Long id) { this.areaId = id; }
    public Long getAreaId() { return areaId; }
    public void setAreaNome(String n) { this.areaNome = n; }
    public String getAreaNome() { return areaNome; }
}