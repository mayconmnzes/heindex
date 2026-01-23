package br.com.heimdex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuração global de CORS.
     * Substituímos IPs fixos por padrões que funcionam tanto localmente quanto na nuvem.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // Configuração para todos os endpoints da API
        registry.addMapping("/**")
            // Permite localhost (desenvolvimento) e qualquer subdomínio do Render/Vercel/Netlify
            .allowedOriginPatterns(
                "http://localhost:[*]",
                "https://*.onrender.com",
                "https://*.vercel.app",
                "*" // Permitir todos temporariamente para evitar bloqueios no deploy inicial
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600); // Cache da pre-flight request por 1 hora
    }
    
    // Nota: addResourceHandlers foi removido pois agora você utiliza Cloudinary (Nuvem)
    // e não deve servir arquivos estáticos de dentro do container do Render.
}