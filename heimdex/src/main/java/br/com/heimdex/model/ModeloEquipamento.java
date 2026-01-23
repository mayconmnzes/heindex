// Código Completo Corrigido - ModeloEquipamento.java
package br.com.heimdex.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // Import correto
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

/**
 * Nova entidade que representa o "Tipo" ou "Modelo" de um equipamento.
 * Ex: "SM471", "Prensa 10T".
 * Peças de reposição serão associadas a este modelo, não à instância
 * específica (Equipamento).
 */
@Entity
@Table(name = "modelos_equipamento")
@Getter
@Setter
public class ModeloEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome; // Ex: "SM471"

    @Column(length = 150)
    private String fabricante; // Ex: "Samsung"

    // O modelo pertence a uma Área
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    // --- CORREÇÃO AQUI ---
    @JsonBackReference("area-modelo") // Adiciona o nome correspondente
    // --- FIM DA CORREÇÃO ---
    private Area area;

    // Lista de instâncias (tags) deste modelo
    @OneToMany(mappedBy = "modelo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // Não precisa de @JsonManagedReference aqui se não formos serializar Equipamento a partir do Modelo
    private Set<Equipamento> equipamentos;

    // Lista de peças associadas a este modelo
    @OneToMany(mappedBy = "modeloEquipamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    // Não precisa de @JsonManagedReference aqui se não formos serializar Peça a partir do Modelo
    private Set<PecaReposicao> pecasAssociadas;
}