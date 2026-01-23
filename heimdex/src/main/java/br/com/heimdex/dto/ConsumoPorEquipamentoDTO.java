// Código Completo Corrigido
package br.com.heimdex.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ConsumoPorEquipamentoDTO {

    private Long equipamentoId;
    private String nomeEquipamento; // Tag (Ex: SM471A)
    private String codigoEquipamento; // Código patrimônio

    // --- NOVOS CAMPOS ADICIONADOS ---
    private String nomeModelo; // Ex: SM471
    private String fabricanteModelo; // Ex: Samsung
    // --- FIM DA ADIÇÃO ---

    private List<PecaConsumidaInfo> pecasConsumidas;

    @Getter
    @Setter
    public static class PecaConsumidaInfo {
        private Long pecaId;
        private String nomePeca;
        private String codigoControle;
        private Long totalConsumido;
    }
}