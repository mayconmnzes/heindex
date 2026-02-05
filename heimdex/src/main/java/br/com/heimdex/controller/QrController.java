package br.com.heimdex.controller;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.QrCodeService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/qrcodes")
public class QrController {

    @Autowired
    private PecaReposicaoRepository pecaRepo;

    @Autowired
    private QrCodeService qrService;

    // Gera QR para a peça e atualiza fotoUrl ou retorna url (não altera fotoUrl automaticamente aqui)
    @PostMapping("/peca/{id}/generate")
    public ResponseEntity<?> generateForPeca(@PathVariable Long id) {
        Optional<PecaReposicao> opt = pecaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        PecaReposicao p = opt.get();

        String codigo = p.getCodigoControle();
        if (codigo == null || codigo.isBlank()) return ResponseEntity.badRequest().body("codigoControle vazio");

        try {
            String filename = codigo + ".png"; // PECA-0001.png
            String url = qrService.generatePngForText(codigo, filename);
            // opcional: salvar o url como fotoUrl ou qrUrl
            p.setFotoUrl(url); // ou setQrUrl se tiver coluna separada
            pecaRepo.save(p);
            return ResponseEntity.ok(url);
        } catch (IOException | WriterException ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    // opcional: endpoint para regenerar QR e apenas retornar a URL sem salvar
    @GetMapping("/peca/{id}")
    public ResponseEntity<?> getOrGenerate(@PathVariable Long id) {
        Optional<PecaReposicao> opt = pecaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        PecaReposicao p = opt.get();
        String codigo = p.getCodigoControle();
        if (codigo == null || codigo.isBlank()) return ResponseEntity.badRequest().body("codigoControle vazio");
        String filename = codigo + ".png";
        try {
            String url = qrService.generatePngForText(codigo, filename);
            return ResponseEntity.ok(url);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }
}