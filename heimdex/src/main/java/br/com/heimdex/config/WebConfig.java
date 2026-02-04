package br.com.heimdex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configuração para servir arquivos da pasta local.
     * Mapeia a URL /uploads/** para a pasta física ./uploads na raiz do projeto.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Pega o caminho da pasta uploads na raiz do projeto
        Path uploadDir = Paths.get("./uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        // Determina o prefixo correto: 'file:///' para Windows ou 'file:' para Linux/Docker
        String protocol = System.getProperty("os.name").toLowerCase().contains("win") ? "file:///" : "file:";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(protocol + uploadPath + "/");
                
        // Mantém sua pasta antiga caso o servidor online a utilize
        registry.addResourceHandler("/uploaded-photos/**")
                .addResourceLocations("file:./uploaded-photos/");
    }

    /**
     * Configuração global de CORS.
     * Essencial para que o sistema funcione quando você voltar para o servidor online.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "http://localhost:[*]",
                    "https://*.onrender.com",
                    "https://*.vercel.app",
                    "*" // Permite origens variadas para facilitar testes
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}