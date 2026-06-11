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

    // ✅ orphanRemoval = true é a correção principal
    // Garante que itens removidos da lista sejam DELETADOS do banco
    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ItemChecklist> itens = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }

    public List<ItemChecklist> getItens() { return itens; }

    // ✅ setItens nunca substitui a referência da lista — sempre limpa e repopula
    // Isso evita que o JPA perca o rastreamento dos itens antigos
    public void setItens(List<ItemChecklist> novosItens) {
        this.itens.clear();
        if (novosItens != null) {
            this.itens.addAll(novosItens);
        }
    }
}
