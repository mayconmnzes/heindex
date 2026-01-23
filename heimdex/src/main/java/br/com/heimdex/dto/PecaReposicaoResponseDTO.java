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
    private String descricaoGenerica;
    private String descricaoTecnica;
    private String aplicacao;
    private String localizacaoPrateleira;
    private int estoqueAtual;
    private int estoqueMinimo;
    
    // ✅ Único campo necessário para fotos online do Cloudinary
    private String fotoUrl; 

    // --- Modelo primário (mantém compatibilidade) ---
    private Long modeloEquipamentoId;
    private String nomeModeloEquipamento; 
    private String fabricanteModeloEquipamento; 

    // Lista de modelos associados
    private List<Long> modelosIds;
}