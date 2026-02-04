package br.com.heimdex;

import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.PerfilUsuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HeimdexApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdexApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                
                // ✅ CORREÇÃO OBRIGATÓRIA: Adicionando o nome para evitar erro de NULL no H2
                admin.setNomeCompleto("Administrador do Sistema"); 
                admin.setMatricula("000001");
                
                admin.setEmail("admin@heimdex.com.br");
                admin.setSenha(passwordEncoder.encode("admin123"));
                
                // ✅ Conversão do Enum para String (ajustado conforme sua Model)
                admin.setPerfil(PerfilUsuario.ADMIN.name()); 
                
                usuarioRepository.save(admin);
                
                // ✅ Mensagem atualizada para o ambiente Local
                System.out.println("✅ Backend Heimdex iniciado com banco H2 Local!");
            }
        };
    }
}