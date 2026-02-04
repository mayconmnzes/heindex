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
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    private CriticidadeEquipamento criticidade;

    @Enumerated(EnumType.STRING)
    private StatusEquipamento status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    @JsonIgnoreProperties({"equipamentos", "hibernateLazyInitializer", "handler"})
    private LinhaDeProducao linha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id", nullable = false)
    @JsonIgnoreProperties({"equipamentos", "pecasAssociadas", "hibernateLazyInitializer", "handler"})
    private ModeloEquipamento modelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Checklist checklistPadrao;

    @Enumerated(EnumType.STRING)
    private FrequenciaPreventiva frequenciaPreventiva;

    @Column(name = "data_ultima_preventiva")
    private LocalDate dataUltimaPreventiva;

    // Getters/Setters Manuais para compatibilidade
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LinhaDeProducao getLinha() { return linha; }
    public void setLinha(LinhaDeProducao linha) { this.linha = linha; }
    public ModeloEquipamento getModelo() { return modelo; }
    public void setModelo(ModeloEquipamento modelo) { this.modelo = modelo; }
}