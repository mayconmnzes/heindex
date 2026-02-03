// Código Completo
package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PecaReposicaoRequestDTO {
    private String nome;
    private String codigoRequisicao;
    private String descricaoGenerica;
    private String descricaoTecnica;
    private String aplicacao;
    private String localizacaoPrateleira;
    private int estoqueAtual;
    private int estoqueMinimo;
    private List<String> fotos;

    // --- ALTERAÇÃO CRUCIAL ---
    // Associamos a peça a um Modelo (Tipo), não a um Equipamento (Instância/Tag)
    private Long modeloEquipamentoId;
    private List<Long> modelosIds;


    // --- REMOVIDO ---
    // private Long equipamentoId;
}