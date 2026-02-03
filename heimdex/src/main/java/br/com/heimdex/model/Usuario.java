package br.com.heimdex.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; 

    @Column(unique = true)
    private String matricula; 

    private String perfil; 

    // MÉTODOS MANUAIS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String n) { this.nomeCompleto = n; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getSenha() { return senha; }
    public void setSenha(String s) { this.senha = s; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String m) { this.matricula = m; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String p) { this.perfil = p; }
}
