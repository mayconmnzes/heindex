package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "planos_manutencao")
@Getter
@Setter
public class PlanoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    // Relacionamento com Equipamento (1 para 1)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipamento_id", unique = true, nullable = false)
    private Equipamento equipamento;

    // Periodicidade em dias (Ex: 30 para Mensal, 90 para Trimestral)
    @Column(nullable = false)
    private Integer periodicidadeDias; 
    
    // Data da próxima execução (usado para o agendamento automático)
    private java.time.LocalDate proximaExecucao;

    // Se a manutenção gera uma OS automaticamente (SIM/NÃO)
    @Column(nullable = false)
    private boolean ativo = true;
}