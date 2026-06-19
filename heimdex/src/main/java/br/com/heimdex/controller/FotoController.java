package br.com.heimdex.controller;

import br.com.heimdex.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFoto(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Nenhum arquivo enviado.");
        }

        try {
            // Envia direto para o Cloudinary (armazenamento permanente)
            // Retorna a URL completa https://res.cloudinary.com/...
            String fileUrl = cloudinaryService.uploadImage(file);
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            System.err.println("Falha ao enviar foto ao Cloudinary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao salvar o arquivo.");
        }
    }
}
