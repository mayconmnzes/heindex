// Código Completo
package br.com.heimdex.dto;

import br.com.heimdex.model.enums.PerfilUsuario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {
    private Long id;
    private String nomeCompleto;
    private String matricula;
    private PerfilUsuario perfil;
}