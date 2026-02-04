package br.com.heimdex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "modelos_equipamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModeloEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 100)
    private String fabricante;

    // ✅ CORREÇÃO: Ignora a lista de modelos dentro da área para evitar o erro de recursão
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    @JsonIgnoreProperties("modelos") 
    private Area area;

    // --- MÉTODOS MANUAIS PARA COMPILAÇÃO ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}