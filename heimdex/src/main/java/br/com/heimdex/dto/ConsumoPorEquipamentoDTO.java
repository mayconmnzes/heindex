package br.com.heimdex.dto;

import java.util.List;

public class ConsumoPorEquipamentoDTO {
    private Long equipamentoId;
    private String nomeEquipamento;
    private String codigoEquipamento;
    private String nomeModelo;
    private String fabricanteModelo;
    private List<PecaConsumidaInfo> pecasConsumidas;

    public static class PecaConsumidaInfo {
        private Long pecaId;
        private String nomePeca;
        private String codigoControle;
        private long totalConsumido;

        public Long getPecaId() { return pecaId; }
        public void setPecaId(Long id) { this.pecaId = id; }
        public String getNomePeca() { return nomePeca; }
        public void setNomePeca(String n) { this.nomePeca = n; }
        public String getCodigoControle() { return codigoControle; }
        public void setCodigoControle(String c) { this.codigoControle = c; }
        public long getTotalConsumido() { return totalConsumido; }
        public void setTotalConsumido(long t) { this.totalConsumido = t; }
    }

    public Long getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(Long id) { this.equipamentoId = id; }
    public String getNomeEquipamento() { return nomeEquipamento; }
    public void setNomeEquipamento(String n) { this.nomeEquipamento = n; }
    public String getCodigoEquipamento() { return codigoEquipamento; }
    public void setCodigoEquipamento(String c) { this.codigoEquipamento = c; }
    public String getNomeModelo() { return nomeModelo; }
    public void setNomeModelo(String n) { this.nomeModelo = n; }
    public String getFabricanteModelo() { return fabricanteModelo; }
    public void setFabricanteModelo(String f) { this.fabricanteModelo = f; }
    public List<PecaConsumidaInfo> getPecasConsumidas() { return pecasConsumidas; }
    public void setPecasConsumidas(List<PecaConsumidaInfo> p) { this.pecasConsumidas = p; }
}
