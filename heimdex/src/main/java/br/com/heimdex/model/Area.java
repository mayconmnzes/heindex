// Código Completo
package br.com.heimdex.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Entity
@Table(name = "areas")
@Getter
@Setter
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    // Relacionamento com Linhas de Produção (Sem alteração)
    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference // Evita loops infinitos de serialização JSON
    private Set<LinhaDeProducao> linhas;

    // --- NOVA ALTERAÇÃO ---
    // Relacionamento com Modelos de Equipamento
    @OneToMany(mappedBy = "area", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("area-modelo") // Nomeia a referência gerenciada
    private Set<ModeloEquipamento> modelos;
}