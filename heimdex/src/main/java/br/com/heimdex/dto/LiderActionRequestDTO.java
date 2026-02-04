package br.com.heimdex.dto;

public class LiderActionRequestDTO {
    private Long liderId;
    private String observacoesLider;

    public Long getLiderId() { return liderId; }
    public void setLiderId(Long liderId) { this.liderId = liderId; }

    public String getObservacoesLider() { return observacoesLider; }
    public void setObservacoesLider(String observacoesLider) { this.observacoesLider = observacoesLider; }
}