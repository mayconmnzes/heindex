package br.com.heimdex.dto;

import java.util.List;

public class FinalizarOsRequestDTO {
    private String observacoesTecnico;
    private List<ResultadoChecklistResponseDTO> resultados;

    public String getObservacoesTecnico() { return observacoesTecnico; }
    public void setObservacoesTecnico(String observacoesTecnico) { this.observacoesTecnico = observacoesTecnico; }

    public List<ResultadoChecklistResponseDTO> getResultados() { return resultados; }
    public void setResultados(List<ResultadoChecklistResponseDTO> resultados) { this.resultados = resultados; }
}