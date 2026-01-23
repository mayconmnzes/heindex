// Código Completo Corrigido - Equipamento.java
package br.com.heimdex.model;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import br.com.heimdex.model.enums.StatusEquipamento;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Import necessário
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "equipamentos")
@Getter
@Setter
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 'nome' agora é a "Tag" ou identificação única. Ex: "SM471A", "SM471B"
    @Column(nullable = false)
    private String nome;

    // 'codigo' permanece como um código de ativo/patrimônio
    @Column(unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private CriticidadeEquipamento criticidade;

    @Enumerated(EnumType.STRING)
    private StatusEquipamento status;

    // --- CORREÇÃO 1: Adicionar JsonIgnoreProperties ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    // Ignora campos que podem causar ciclos ou carregar dados demais via JSON
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "area", "equipamentos"}) // Adicionado 'equipamentos' para evitar ciclo Linha->Area->Linha
    private LinhaDeProducao linha;

    // --- CORREÇÃO 2: Adicionar JsonIgnoreProperties ---
    @ManyToOne(fetch = FetchType.LAZY) // Já era LAZY
    @JoinColumn(name = "checklist_id")
    // Ignora campos que podem causar ciclos
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "area", "itens"})
    private Checklist checklistPadrao;

    // --- CORREÇÃO 3: Mudar para LAZY e Adicionar JsonIgnoreProperties ---
    @ManyToOne(fetch = FetchType.LAZY) // ALTERADO DE EAGER PARA LAZY
    @JoinColumn(name = "modelo_id", nullable = false)
    // Ignora campos do Modelo que podem levar a ciclos profundos
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipamentos", "pecasAssociadas", "area"})
    private ModeloEquipamento modelo;

    // --- Campos de Preventiva (Sem alteração) ---
    @Enumerated(EnumType.STRING)
    private FrequenciaPreventiva frequenciaPreventiva;

    private LocalDate dataUltimaPreventiva;
}