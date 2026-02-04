package br.com.heimdex.model;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "equipamentos")
@NoArgsConstructor
@AllArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String nome;

    @Column(nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "checklist_id")
    private Checklist checklist;

    @Enumerated(EnumType.STRING)
    private CriticidadeEquipamento criticidade;

    @Enumerated(EnumType.STRING)
    private FrequenciaPreventiva frequenciaPreventiva;

    private LocalDate dataUltimaPreventiva;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id")
    private LinhaDeProducao linha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private ModeloEquipamento modelo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Checklist getChecklist() { return checklist; }
    public void setChecklist(Checklist checklist) { this.checklist = checklist; }
    public CriticidadeEquipamento getCriticidade() { return criticidade; }
    public void setCriticidade(CriticidadeEquipamento criticidade) { this.criticidade = criticidade; }
    public FrequenciaPreventiva getFrequenciaPreventiva() { return frequenciaPreventiva; }
    public void setFrequenciaPreventiva(FrequenciaPreventiva frequenciaPreventiva) { this.frequenciaPreventiva = frequenciaPreventiva; }
    public LocalDate getDataUltimaPreventiva() { return dataUltimaPreventiva; }
    public void setDataUltimaPreventiva(LocalDate dataUltimaPreventiva) { this.dataUltimaPreventiva = dataUltimaPreventiva; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LinhaDeProducao getLinha() { return linha; }
    public void setLinha(LinhaDeProducao linha) { this.linha = linha; }
    public ModeloEquipamento getModelo() { return modelo; }
    public void setModelo(ModeloEquipamento modelo) { this.modelo = modelo; }
}