package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "fotos_os")
@Getter
@Setter
public class FotoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    // URL onde a foto está armazenada (Ex: /simulacao-fotos/abc-123.jpg)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String url; 

    // Descrição ou observação opcional
    private String descricao;
}