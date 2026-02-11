package br.com.heimdex;

import br.com.heimdex.model.Usuario;
import br.com.heimdex.model.enums.PerfilUsuario;
import br.com.heimdex.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HeimdexApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdexApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Environment env) {
        return args -> {
            // Detectar qual banco está sendo usado
            String datasourceUrl = env.getProperty("spring.datasource.url");
            boolean isMySQL = datasourceUrl != null && datasourceUrl.contains("mysql");
            boolean isH2 = datasourceUrl != null && datasourceUrl.contains("h2");
            
            System.out.println("========================================");
            if (isMySQL) {
                System.out.println("✅ Backend Heimdex conectado ao MySQL (Aiven)");
                // Mask passwords in both query string and URL authority formats
                String maskedUrl = datasourceUrl.replaceAll("password=[^&]*", "password=***")
                                                .replaceAll(":[^:@]+@", ":***@");
                System.out.println("🔗 Database: " + maskedUrl);
            } else if (isH2) {
                System.out.println("⚠️  Backend Heimdex usando banco H2 Local (desenvolvimento)");
                System.out.println("⚠️  ATENÇÃO: Configure as variáveis DB_URL, DB_USER, DB_PASSWORD no Render!");
            } else {
                System.out.println("⚠️  Backend Heimdex - Banco não identificado");
            }
            System.out.println("========================================");
            
            // Criar usuário admin apenas se não existir
            if (usuarioRepository.findByMatricula("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNomeCompleto("Administrador Padrão");
                admin.setMatricula("admin");
                admin.setSenha(passwordEncoder.encode("admin"));
                admin.setPerfil(PerfilUsuario.ADMINISTRADOR.name());
                admin.setEmail("admin@heimdex.com");
                
                usuarioRepository.save(admin);
                System.out.println(">>> ✅ Usuário admin criado com sucesso!");
                System.out.println(">>> 📋 Login: admin");
                System.out.println(">>> 🔑 Senha: admin");
            } else {
                System.out.println(">>> ℹ️  Usuário admin já existe no banco");
            }
        };
    }
}