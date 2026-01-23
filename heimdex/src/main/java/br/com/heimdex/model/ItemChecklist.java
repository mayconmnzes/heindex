package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set; // MUDOU

@Entity
@Table(name = "itens_checklist")
@Getter
@Setter
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
    private Set<PecaReposicao> pecasSugeridas; // MUDOU PARA SET
}