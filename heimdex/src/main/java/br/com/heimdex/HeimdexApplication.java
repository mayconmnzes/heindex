package br.com.heimdex;

import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.PerfilUsuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class HeimdexApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdexApplication.class, args);
    }
    @Bean
    CommandLineRunner run(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Cria o usuário ADMIN se ele não existir
            if (usuarioRepository.findByMatricula("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNomeCompleto("Administrador Padrão");
                admin.setMatricula("admin");
                admin.setSenha(passwordEncoder.encode("admin"));
                admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
                usuarioRepository.save(admin);
                System.out.println(">>> Usuário admin padrão criado: admin/admin");
            }
            
            // Cria o usuário TÉCNICO se ele não existir
            if (usuarioRepository.findByMatricula("tecnico1").isEmpty()) {
                Usuario tecnico = new Usuario();
                tecnico.setNomeCompleto("Técnico Exemplo");
                tecnico.setMatricula("tecnico1");
                tecnico.setSenha(passwordEncoder.encode("tecnico1"));
                tecnico.setPerfil(PerfilUsuario.TECNICO);
                usuarioRepository.save(tecnico);
                System.out.println(">>> Usuário tecnico1 padrão criado: tecnico1/tecnico1");
            }
        };
    }
}
