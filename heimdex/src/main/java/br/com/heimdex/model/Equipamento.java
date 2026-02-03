package br.com.heimdex.model;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import br.com.heimdex.model.enums.StatusEquipamento;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "equipamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "codigo", unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "criticidade", length = 20)
    private CriticidadeEquipamento criticidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StatusEquipamento status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "area", "equipamentos"})
    private LinhaDeProducao linha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "area", "itens"})
    private Checklist checklistPadrao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipamentos", "pecasAssociadas", "area"})
    private ModeloEquipamento modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequencia_preventiva", length = 30)
    private FrequenciaPreventiva frequenciaPreventiva;

    @Column(name = "data_ultima_preventiva")
    private LocalDate dataUltimaPreventiva;

    // --- MÉTODOS MANUAIS PARA COMPILAÇÃO ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public CriticidadeEquipamento getCriticidade() { return criticidade; }
    public void setCriticidade(CriticidadeEquipamento criticidade) { this.criticidade = criticidade; }
    public LinhaDeProducao getLinha() { return linha; }
    public void setLinha(LinhaDeProducao linha) { this.linha = linha; }
    public Checklist getChecklistPadrao() { return checklistPadrao; }
    public void setChecklistPadrao(Checklist checklistPadrao) { this.checklistPadrao = checklistPadrao; }
    public ModeloEquipamento getModelo() { return modelo; }
    public void setModelo(ModeloEquipamento modelo) { this.modelo = modelo; }
    public FrequenciaPreventiva getFrequenciaPreventiva() { return frequenciaPreventiva; }
    public void setFrequenciaPreventiva(FrequenciaPreventiva frequenciaPreventiva) { this.frequenciaPreventiva = frequenciaPreventiva; }
    public LocalDate getDataUltimaPreventiva() { return dataUltimaPreventiva; }
    public void setDataUltimaPreventiva(LocalDate dataUltimaPreventiva) { this.dataUltimaPreventiva = dataUltimaPreventiva; }
}
