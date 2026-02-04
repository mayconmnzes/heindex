package br.com.heimdex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Define o encoder para as senhas (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura as regras de segurança HTTP, CORS e Liberação de Recursos.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Habilita CORS configurado no WebConfig (Vercel/Render/Local)
            .cors(withDefaults())

            // 2. Configurações para o H2 Console funcionar localmente
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) 
            
            // 3. Desabilita CSRF para APIs REST Stateless e ignora para o H2
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
                .disable()
            )
            
            // 4. Define política de sessão como Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 5. Configuração das regras de autorização
            .authorizeHttpRequests(authorize -> authorize
                // Libera o Console do Banco de Dados H2
                .requestMatchers("/h2-console/**").permitAll()
                
                // ✅ LIBERAÇÃO DE IMAGENS: Essencial para Foto e QR Code aparecerem
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/uploaded-photos/**").permitAll()
                
                // Libera endpoints de monitoramento (Essencial para o Render)
                .requestMatchers("/actuator/**").permitAll()
                
                // Libera todos os endpoints da API (Incluindo QR Code em /api/pecas/*/qrcode)
                .requestMatchers("/api/**").permitAll()
                
                // Permite acesso irrestrito a qualquer outra URL necessária
                .anyRequest().permitAll() 
            );

        return http.build();
    }
}