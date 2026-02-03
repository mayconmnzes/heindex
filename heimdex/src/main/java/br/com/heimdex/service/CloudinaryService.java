package br.com.heimdex.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // Construtor que lê as credenciais do seu application.properties
    public CloudinaryService(@Value("${cloudinary.cloud_name}") String cloudName,
                             @Value("${cloudinary.api_key}") String apiKey,
                             @Value("${cloudinary.api_secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    /**
     * Envia a imagem para o Cloudinary e retorna a URL pública.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Usamos um mapa vazio para o upload básico, o que evita erros de assinatura por parâmetros extras
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), com.cloudinary.utils.ObjectUtils.emptyMap());
        
        // Retorna a URL segura (https) gerada automaticamente
        return uploadResult.get("secure_url").toString();
    }
}