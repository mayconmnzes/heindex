// Código Completo
package br.com.heimdex;

import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.PerfilUsuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling; // NOVO IMPORT
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling // <-- HABILITA O AGENDAMENTO DE TAREFAS
public class HeimdexApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdexApplication.class, args);
    }

    // Código CommandLineRunner (criação de usuários) permanece o mesmo...
    @Bean
    CommandLineRunner run(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // --- Cria o usuário ADMIN se ele não existir ---
            if (usuarioRepository.findByMatricula("admin").isEmpty()) {
                System.out.println(">>> Criando usuário admin padrão...");
                Usuario admin = new Usuario();
                admin.setNomeCompleto("Administrador Padrão");
                admin.setMatricula("admin");
                admin.setSenha(passwordEncoder.encode("admin"));
                admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
                usuarioRepository.save(admin);
                System.out.println(">>> Usuário admin padrão criado com sucesso. Matrícula: admin | Senha: admin");
            }
            // --- Cria o usuário TÉCNICO se ele não existir ---
            if (usuarioRepository.findByMatricula("tecnico1").isEmpty()) {
                System.out.println(">>> Criando usuário tecnico padrão...");
                Usuario tecnico = new Usuario();
                tecnico.setNomeCompleto("Técnico Exemplo");
                tecnico.setMatricula("tecnico1");
                tecnico.setSenha(passwordEncoder.encode("tecnico1"));
                tecnico.setPerfil(PerfilUsuario.TECNICO);
                usuarioRepository.save(tecnico);
                System.out.println(">>> Usuário tecnico1 padrão criado com sucesso. Matrícula: tecnico1 | Senha: tecnico1");
            }
        };
    }
}