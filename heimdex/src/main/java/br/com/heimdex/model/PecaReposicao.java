package br.com.heimdex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "pecas_reposicao")
@Getter
@Setter
public class PecaReposicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@ElementCollection(fetch = FetchType.EAGER)
    //@CollectionTable(name = "peca_modelos_ids", joinColumns = @JoinColumn(name = "peca_id"))
    //@Column(name = "modelo_id")
    //private List<Long> modelosIds;

    @Column(unique = true, length = 50)
    private String codigoControle;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 100)
    private String codigoRequisicao;

    @Column(columnDefinition = "TEXT")
    private String descricaoGenerica;

    @Column(columnDefinition = "TEXT")
    private String descricaoTecnica;

    @Column(length = 255)
    private String aplicacao;

    @Column(length = 100)
    private String localizacaoPrateleira;

    @Column(nullable = false)
    private int estoqueAtual;

    @Column(nullable = false)
    private int estoqueMinimo;

    // ✅ Campo principal para o link Cloudinary
    @Column(length = 500)
    private String fotoUrl;

    /* ❌ Removido para evitar erro "Table peca_fotos doesn't exist" no Aiven
       @ElementCollection(fetch = FetchType.EAGER)
       @CollectionTable(name = "peca_fotos", joinColumns = @JoinColumn(name = "peca_id"))
       @Column(name = "foto_url")
       private List<String> fotos; 
    */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_equipamento_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pecasAssociadas", "equipamentos", "area"})
    private ModeloEquipamento modeloEquipamento;
}