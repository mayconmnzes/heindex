package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "itens_checklist")
@NoArgsConstructor
@AllArgsConstructor
public class ItemChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "item_pecas_sugeridas",
        joinColumns = @JoinColumn(name = "item_checklist_id"),
        inverseJoinColumns = @JoinColumn(name = "peca_id")
    )
    private Set<PecaReposicao> pecasSugeridas;

    // --- MÉTODOS MANUAIS PARA GARANTIR COMPILAÇÃO NO DOCKER ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Checklist getChecklist() {
        return checklist;
    }

    public void setChecklist(Checklist checklist) {
        this.checklist = checklist;
    }

    public Set<PecaReposicao> getPecasSugeridas() {
        return pecasSugeridas;
    }

    public void setPecasSugeridas(Set<PecaReposicao> pecasSugeridas) {
        this.pecasSugeridas = pecasSugeridas;
    }
}
