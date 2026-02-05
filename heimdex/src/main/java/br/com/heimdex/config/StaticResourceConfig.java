package br.com.heimdex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.qrcode.dir:uploads/qrcodes}")
    private String qrcodeDir;

    @Value("${app.qrcode.base-url:/uploads/qrcodes}")
    private String qrcodeBaseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Converte para caminho absoluto e prefixa com file:
        String absolutePath = Paths.get(qrcodeDir).toAbsolutePath().toString().replace("\\", "/");
        String resourceLocation = "file:" + absolutePath + "/"; // importante terminar com slash
        registry.addResourceHandler(qrcodeBaseUrl + "/**")
                .addResourceLocations(resourceLocation);
    }
}