package br.com.heimdex.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "checklists")
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ItemChecklist> itens = new ArrayList<>();

    // MÉTODOS MANUAIS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
    public List<ItemChecklist> getItens() { return itens; }
    public void setItens(List<ItemChecklist> itens) { this.itens = itens; }
}