package br.com.heimdex.dto;

public class UsuarioDTO {
    private Long id;
    private String nomeCompleto;
    private String matricula;
    private String perfil;
    private String token;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String n) { this.nomeCompleto = n; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String m) { this.matricula = m; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String p) { this.perfil = p; }
    public String getToken() { return token; }
    public void setToken(String t) { this.token = t; }
}
