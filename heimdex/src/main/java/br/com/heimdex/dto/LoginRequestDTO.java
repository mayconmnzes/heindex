package br.com.heimdex.dto;

public class LoginRequestDTO {
    private String matricula;
    private String senha;

    public String getMatricula() { return matricula; }
    public void setMatricula(String m) { this.matricula = m; }
    public String getSenha() { return senha; }
    public void setSenha(String s) { this.senha = s; }
}