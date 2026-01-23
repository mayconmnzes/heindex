// Código Completo
package br.com.heimdex.model;

import br.com.heimdex.model.enums.PerfilUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Representa um usuário do sistema, com suas credenciais e perfil de acesso.
 * Esta classe será mapeada para a tabela "usuarios" no banco de dados.
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nomeCompleto;

    @Column(nullable = false, unique = true, length = 50)
    private String matricula;

    @Column(nullable = false)
    private String senha;

    // @Enumerated diz ao JPA para salvar o texto do Enum (ex: "ADMINISTRADOR")
    // em vez de um número (0, 1, 2). É melhor para a clareza do banco de dados.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerfilUsuario perfil;

}