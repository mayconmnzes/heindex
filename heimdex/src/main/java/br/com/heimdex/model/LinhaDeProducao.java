package br.com.heimdex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "linhas_de_producao")
@NoArgsConstructor
@AllArgsConstructor
public class LinhaDeProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    // ✅ CORREÇÃO: Usando ignore para permitir visualização da área sem loop
    @JsonIgnoreProperties({"linhas", "modelos"}) 
    private Area area;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}