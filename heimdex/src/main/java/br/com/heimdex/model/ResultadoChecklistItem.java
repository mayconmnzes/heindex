package br.com.heimdex.model;

import br.com.heimdex.model.enums.ItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "resultados_checklist")
@Getter
@Setter
public class ResultadoChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER para carregar o template do item junto com o resultado.
    @ManyToOne(fetch = FetchType.EAGER) // CRÍTICO: Deve ser EAGER
    @JoinColumn(name = "item_template_id", nullable = false)
    private ItemChecklist itemTemplate; 

    // EAGER para garantir a consistência no carregamento.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}