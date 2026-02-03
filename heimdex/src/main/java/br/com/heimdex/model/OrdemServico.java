package br.com.heimdex.model;

import br.com.heimdex.model.enums.StatusOrdemServico;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ordens_servico")
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "os_tecnicos_executores",
        joinColumns = @JoinColumn(name = "ordem_servico_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> tecnicosExecutores = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lider_validador_id")
    private Usuario liderValidador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    private Checklist checklist;

    @OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ResultadoChecklistItem> resultados = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;
    
    @Column(length = 50)
    private String tipoManutencao;

    private LocalDateTime dataAgendamento;
    private LocalDateTime dataInicioExecucao;
    private LocalDateTime dataFimExecucao;
    private LocalDateTime dataValidacao;

    @Column(columnDefinition = "TEXT")
    private String observacoesTecnico;

    @Column(columnDefinition = "TEXT")
    private String observacoesLider;

    // --- MÉTODOS MANUAIS DE ACESSO (BLINDAGEM DOCKER) ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }

    public Set<Usuario> getTecnicosExecutores() { return tecnicosExecutores; }
    public void setTecnicosExecutores(Set<Usuario> tecnicosExecutores) { this.tecnicosExecutores = tecnicosExecutores; }

    public Checklist getChecklist() { return checklist; }
    public void setChecklist(Checklist checklist) { this.checklist = checklist; }

    public Set<ResultadoChecklistItem> getResultados() { return resultados; }
    public void setResultados(Set<ResultadoChecklistItem> resultados) { this.resultados = resultados; }

    public StatusOrdemServico getStatus() { return status; }
    public void setStatus(StatusOrdemServico status) { this.status = status; }

    public String getTipoManutencao() { return tipoManutencao; }
    public void setTipoManutencao(String tipoManutencao) { this.tipoManutencao = tipoManutencao; }

    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }

    public LocalDateTime getDataInicioExecucao() { return dataInicioExecucao; }
    public void setDataInicioExecucao(LocalDateTime dataInicioExecucao) { this.dataInicioExecucao = dataInicioExecucao; }

    public LocalDateTime getDataFimExecucao() { return dataFimExecucao; }
    public void setDataFimExecucao(LocalDateTime dataFimExecucao) { this.dataFimExecucao = dataFimExecucao; }

    // Método para suportar chamadas .builder() caso o service use
    public static OrdemServico builder() { return new OrdemServico(); }

    public void addResultado(ResultadoChecklistItem resultado) {
        this.resultados.add(resultado);
        resultado.setOrdemServico(this);
    }
}
