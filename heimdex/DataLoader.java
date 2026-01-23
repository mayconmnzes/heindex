// Código Completo
package br.com.heimdex;

import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.PerfilUsuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Esta verificação garante que o código só rode se não houver um usuário 'admin'
        if (usuarioRepository.findByMatricula("admin").isEmpty()) {
            System.out.println("Nenhum usuário admin encontrado, criando usuário padrão...");

            Usuario admin = new Usuario();
            admin.setNomeCompleto("Administrador Padrão");
            admin.setMatricula("admin");
            admin.setSenha(passwordEncoder.encode("admin")); // Criptografa a senha 'admin'
            admin.setPerfil(PerfilUsuario.ADMINISTRADOR);

            usuarioRepository.save(admin);
            System.out.println("Usuário admin padrão criado com sucesso. Matrícula: admin | Senha: admin");
        }
    }
}