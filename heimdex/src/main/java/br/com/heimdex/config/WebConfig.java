package br.com.heimdex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry; // Import adicionado para corrigir o erro
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuração para permitir que o Spring sirva arquivos da pasta local.
     * Importante: No Render, esses arquivos somem ao reiniciar se não houver Disk persistente.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploaded-photos/**")
                .addResourceLocations("file:./uploaded-photos/");
    }

    /**
     * Configuração global de CORS.
     * Permite que o seu Frontend (Vercel) acesse o seu Backend (Render).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configuração para todos os endpoints da API
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:[*]",
                    "https://*.onrender.com",
                    "https://*.vercel.app",
                    "*" 
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
