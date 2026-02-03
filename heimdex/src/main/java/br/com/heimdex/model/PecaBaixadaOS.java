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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaReposicao peca;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_checklist_id")
    private ItemChecklist itemSugerido; 

    @Column(nullable = false)
    private int quantidadeBaixada;

    @Column(nullable = false)
    private LocalDateTime dataBaixa = LocalDateTime.now();
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_estoque_id")
    private MovimentacaoEstoque movimentacao;

    // SETTERS MANUAIS PARA COMPILAÇÃO GARANTIDA NO RENDER
    public void setOrdemServico(OrdemServico os) { this.ordemServico = os; }
    public void setPeca(PecaReposicao p) { this.peca = p; }
    public void setQuantidadeBaixada(int q) { this.quantidadeBaixada = q; }
    public void setMovimentacao(MovimentacaoEstoque m) { this.movimentacao = m; }
}