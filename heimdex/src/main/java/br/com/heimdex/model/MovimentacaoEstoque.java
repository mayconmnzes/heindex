package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipoMovimentacao; 

    @ManyToOne(fetch = FetchType.EAGER) // Carregamento imediato para evitar erro 500
    @JoinColumn(name = "peca_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Evita erro de proxy do Jackson
    private PecaReposicao peca;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private LocalDateTime dataMovimentacao = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String observacao;
}