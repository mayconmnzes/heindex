package br.com.heimdex.controller;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.QrCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/qrcodes")
public class QrController {

    private static final Logger log = LoggerFactory.getLogger(QrController.class);

    @Autowired
    private PecaReposicaoRepository pecaRepo;

    @Autowired
    private QrCodeService qrService;

    /**
     * Gera QR para a peça e salva a URL em qr_url (NÃO sobrescreve foto_url).
     * POST /api/qrcodes/peca/{id}/generate
     */
    @PostMapping("/peca/{id}/generate")
    public ResponseEntity<?> generateForPeca(@PathVariable Long id) {
        Optional<PecaReposicao> opt = pecaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        PecaReposicao p = opt.get();

        // preferir codigoControle, fallback para codigoRequisicao
        String codigo = (p.getCodigoControle() != null && !p.getCodigoControle().isBlank())
                ? p.getCodigoControle()
                : p.getCodigoRequisicao();

        if (codigo == null || codigo.isBlank()) return ResponseEntity.badRequest().body("Sem código disponível para gerar QR");

        try {
            log.info("Gerando QR (generateForPeca) para id={} texto={}", p.getId(), codigo);
            String filename = codigo + ".png"; // PECA-0001.png ou fallback
            String url = qrService.generatePngForText(codigo, filename);
            // salva em qr_url para não tocar em foto_url
            p.setQrUrl(url);
            pecaRepo.save(p);
            return ResponseEntity.ok(url);
        } catch (IOException | WriterException ex) {
            log.error("Erro gerando QR para id={} : {}", p.getId(), ex.getMessage(), ex);
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    /**
     * Gera um QR on-demand e retorna a URL sem salvar (útil para preview).
     * GET /api/qrcodes/peca/{id}
     */
    @GetMapping("/peca/{id}")
    public ResponseEntity<?> getOrGenerate(@PathVariable Long id) {
        Optional<PecaReposicao> opt = pecaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        PecaReposicao p = opt.get();
        String codigo = (p.getCodigoControle() != null && !p.getCodigoControle().isBlank())
                ? p.getCodigoControle()
                : p.getCodigoRequisicao();
        if (codigo == null || codigo.isBlank()) return ResponseEntity.badRequest().body("Sem código disponível para gerar QR");
        String filename = codigo + ".png";
        try {
            log.info("Gerando QR (getOrGenerate) para id={} texto={}", p.getId(), codigo);
            String url = qrService.generatePngForText(codigo, filename);
            return ResponseEntity.ok(url);
        } catch (Exception ex) {
            log.error("Erro gerando QR preview para id={} : {}", p.getId(), ex.getMessage(), ex);
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    /**
     * Regenera e salva o QR da peça — similar a /generate, mapeado para GET para facilitar testes.
     * GET /api/qrcodes/peca/{id}/regenerate
     */
    @GetMapping("/peca/{id}/regenerate")
    public ResponseEntity<?> regenerateForPeca(@PathVariable Long id) {
        Optional<PecaReposicao> opt = pecaRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        PecaReposicao p = opt.get();

        String codigo = (p.getCodigoControle() != null && !p.getCodigoControle().isBlank())
                ? p.getCodigoControle()
                : p.getCodigoRequisicao();

        if (codigo == null || codigo.isBlank()) return ResponseEntity.badRequest().body("Sem código disponível para gerar QR");

        try {
            log.info("Regenerando QR para id={} texto={}", p.getId(), codigo);
            String filename = codigo + ".png";
            String url = qrService.generatePngForText(codigo, filename);
            p.setQrUrl(url);
            pecaRepo.save(p);
            return ResponseEntity.ok(url);
        } catch (IOException | WriterException ex) {
            log.error("Erro regenerando QR para id={} : {}", p.getId(), ex.getMessage(), ex);
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }
}