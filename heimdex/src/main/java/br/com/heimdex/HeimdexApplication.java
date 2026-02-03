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
                // ✅ Ajustado para garantir compatibilidade com a String
                admin.setEmail("admin@heimdex.com.br");
                admin.setSenha(passwordEncoder.encode("admin123"));
                
                // ✅ Se sua Model espera String, usamos .name() para converter o Enum
                // Se o erro do setNome persistir, verifique o nome do campo na sua classe Usuario.java
                admin.setPerfil(PerfilUsuario.ADMIN.name()); 
                
                usuarioRepository.save(admin);
                System.out.println("✅ Backend Heimdex iniciado e conectado ao Aiven!");
            }
        };
    }
}