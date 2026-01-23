package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "pecas_baixadas_os")
@Getter
@Setter
public class PecaBaixadaOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A qual Ordem de Serviço esta peça foi baixada
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    // A Peça de Reposição que foi usada
    @ManyToOne(fetch = FetchType.EAGER) // EAGER para facilitar o relatório
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaReposicao peca;
    
    // Opcional: Referência ao Item Checklist que sugeriu essa peça
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_checklist_id")
    private ItemChecklist itemSugerido; 

    @Column(nullable = false)
    private int quantidadeBaixada;

    @Column(nullable = false)
    private LocalDateTime dataBaixa = LocalDateTime.now();
    
    // Opcional: Referência à MovimentacaoEstoque de SAÍDA que deu a baixa real no estoque
    // Isto garante rastreabilidade total
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_estoque_id")
    private MovimentacaoEstoque movimentacao;
}