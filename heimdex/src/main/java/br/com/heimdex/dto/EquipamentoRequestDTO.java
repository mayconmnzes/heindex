package br.com.heimdex.dto;

import br.com.heimdex.model.enums.CriticidadeEquipamento;
import br.com.heimdex.model.enums.FrequenciaPreventiva;
import java.time.LocalDate;

public record EquipamentoRequestDTO(
    String nome,
    String codigo,
    Long linhaId,
    Long modeloId,
    Long checklistId,
    CriticidadeEquipamento criticidade,
    FrequenciaPreventiva frequenciaPreventiva,
    LocalDate dataUltimaPreventiva
) {
    // Métodos de compatibilidade para o Controller não dar erro de "symbol not found"
    public String getNome() { return nome; }
    public String getCodigo() { return codigo; }
    public Long getLinhaId() { return linhaId; }
    public Long getModeloId() { return modeloId; }
    public Long getChecklistId() { return checklistId; }
    public CriticidadeEquipamento getCriticidade() { return criticidade; }
    public FrequenciaPreventiva getFrequenciaPreventiva() { return frequenciaPreventiva; }
    public LocalDate getDataUltimaPreventiva() { return dataUltimaPreventiva; }
}