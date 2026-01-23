package br.com.heimdex.model;

import br.com.heimdex.model.enums.StatusOrdemServico;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ordens_servico")
@Getter
@Setter
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... (outros campos como equipamento, tecnico, etc. permanecem iguais) ...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_executor_id", nullable = false)
    private Usuario tecnicoExecutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lider_validador_id")
    private Usuario liderValidador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    private Checklist checklist;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ResultadoChecklistItem> resultados = new HashSet<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<FotoOS> fotosEvidencia = new HashSet<>();

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PecaBaixadaOS> pecasConsumidas = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;
    
    // --- NOVO CAMPO ---
    @Column(length = 50)
    private String tipoManutencao; // Ex: "PREVENTIVA", "CORRETIVA", "CALIBRACAO"

    private LocalDateTime dataAgendamento;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
    private LocalDateTime dataValidacao;

    @Column(columnDefinition = "TEXT")
    private String observacoesTecnico;

    @Column(columnDefinition = "TEXT")
    private String observacoesLider;
}