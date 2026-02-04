package br.com.heimdex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rootPath = System.getProperty("user.dir");
        String os = System.getProperty("os.name").toLowerCase();
        
        // Protocolo dinâmico para Windows vs Linux
        String protocol = os.contains("win") ? "file:///" : "file:";
        
        // Caminhos absolutos
        String uploadLocation = protocol + rootPath + File.separator + "uploads" + File.separator;
        String legacyLocation = protocol + rootPath + File.separator + "uploaded-photos" + File.separator;

        // ✅ Configuração para /uploads com correção de bloqueio ORB
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(0)
                .resourceChain(true) // Ajuda o navegador a validar o recurso
                .addResolver(new PathResourceResolver());

        // ✅ Suporte para pasta antiga
        registry.addResourceHandler("/uploaded-photos/**")
                .addResourceLocations(legacyLocation)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
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