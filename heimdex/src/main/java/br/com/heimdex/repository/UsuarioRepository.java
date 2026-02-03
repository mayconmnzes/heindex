// Código Completo
package br.com.heimdex.repository;

import br.com.heimdex.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // O Spring cria o SQL automaticamente só pelo nome do método!
    // "Encontre um usuário pela sua matrícula"
    Optional<Usuario> findByMatricula(String matricula);
}