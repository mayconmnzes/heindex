package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PecaReposicaoResponseDTO {
    private Long id;
    private String codigoControle;
    private String nome;
    private String codigoRequisicao;
    private String descricaoTecnica;
    private String aplicacao;
    private String localizacaoPrateleira;
    private Integer estoqueAtual;
    private Integer estoqueMinimo;
    private String fotoUrl;
    private Long modeloEquipamentoId;
    private String nomeModeloEquipamento;
    private String fabricanteModeloEquipamento;

    // ✅ Métodos manuais para garantir que o RelatorioController compile mesmo se o Lombok oscilar
    public void setEstoqueMinimo(Integer e) { this.estoqueMinimo = e; }
    public void setLocalizacaoPrateleira(String l) { this.localizacaoPrateleira = l; }
    public void setFotoUrl(String f) { this.fotoUrl = f; }
    public void setModeloEquipamentoId(Long id) { this.modeloEquipamentoId = id; }
    public void setNomeModeloEquipamento(String n) { this.nomeModeloEquipamento = n; }
    public void setFabricanteModeloEquipamento(String f) { this.fabricanteModeloEquipamento = f; }
}