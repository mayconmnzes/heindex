package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set; // MUDOU

@Entity
@Table(name = "checklists")
@Getter
@Setter
public class Checklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @ManyToOne 
    @JoinColumn(name = "area_id", nullable = false)
    private Area area;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "checklist", fetch = FetchType.EAGER)
    private Set<ItemChecklist> itens; // MUDOU PARA SET
}