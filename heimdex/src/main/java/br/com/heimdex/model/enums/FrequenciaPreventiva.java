package br.com.heimdex.model.enums;

public enum FrequenciaPreventiva {
    QUINZENAL(15), MENSAL(30), TRIMESTRAL(90), SEMESTRAL(180), ANUAL(365);

    private final int dias;
    FrequenciaPreventiva(int dias) { this.dias = dias; }
    public int getDias() { return dias; } // Método Manual para o Service
}
