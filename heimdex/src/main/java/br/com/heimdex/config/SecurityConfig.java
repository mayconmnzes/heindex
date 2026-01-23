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
     * Configura as regras de segurança HTTP e CORS.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilita CORS configurado no WebConfig
            .cors(withDefaults())
            
            // Desabilita CSRF para APIs REST Stateless
            .csrf(csrf -> csrf.disable())
            
            // Define política de sessão como Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configuração das regras de autorização
            .authorizeHttpRequests(authorize -> authorize
                // Libera endpoints de monitoramento (Essencial para o Render não dormir)
                .requestMatchers("/actuator/**").permitAll()
                
                // Libera todos os endpoints da API
                .requestMatchers("/api/**").permitAll()
                
                // Permite acesso irrestrito a qualquer outra URL necessária
                .anyRequest().permitAll() 
            );

        return http.build();
    }
}