package br.com.heimdex.controller;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.PecaService;
import br.com.heimdex.service.QrCodeService;
import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private static final Logger log = LoggerFactory.getLogger(PecaController.class);
    
    // Regex patterns for ID extraction from QR codes and user input
    // Matches formats like: "ID-19", "PECA-0001", "PIECE-123"
    private static final String PREFIXED_ID_PATTERN = "^[A-Za-z]+-\\d+$";
    // Matches numeric IDs only: "19", "123", "0001"
    private static final String NUMERIC_ID_PATTERN = "^\\d+$";

    @Autowired
    private PecaReposicaoRepository pecaReposicaoRepository;

    @Autowired
    private PecaService pecaService;

    @Autowired
    private QrCodeService qrService;

    private boolean isInvalidCodigoControle(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.isEmpty()) return true;
        // tratar casos comuns vindos do front: "null", "undefined"
        if (t.equalsIgnoreCase("null") || t.equalsIgnoreCase("undefined")) return true;
        return false;
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookupPeca(@RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Parâmetro 'code' obrigatório");
        }
        String c = code.trim();
        
        log.info("🔍 Buscando peça com código: '{}'", c);

        // ========================================
        // PRIORIDADE 1: Busca por ID numérico
        // ========================================
        // Aceita formatos: "ID-19", "PECA-19", "19"
        String idExtracted = null;
        
        // Extrai número de formatos como "ID-19", "PECA-0001", etc
        if (c.matches(PREFIXED_ID_PATTERN)) {
            // Formato: "ID-19", "PECA-0001"
            idExtracted = c.replaceAll("^[A-Za-z]+-", "");
        } else if (c.matches(NUMERIC_ID_PATTERN)) {
            // Formato: "19" (só números)
            idExtracted = c;
        }
        
        if (idExtracted != null) {
            try {
                Long id = Long.parseLong(idExtracted);
                Optional<PecaReposicao> byId = pecaReposicaoRepository.findById(id);
                if (byId.isPresent()) {
                    log.info("✅ Peça encontrada por ID: {}", id);
                    return ResponseEntity.ok(byId.get());
                }
            } catch (NumberFormatException e) {
                log.warn("⚠️ Não foi possível converter '{}' para ID numérico", idExtracted);
            }
        }

        // ========================================
        // PRIORIDADE 2: Busca exata por codigo_controle
        // ========================================
        Optional<PecaReposicao> opt = pecaReposicaoRepository.findByCodigoControleIgnoreCase(c);
        if (opt.isPresent()) {
            log.info("✅ Peça encontrada por codigo_controle: {}", c);
            return ResponseEntity.ok(opt.get());
        }

        // ========================================
        // PRIORIDADE 3: Busca exata por codigo_requisicao
        // ========================================
        opt = pecaReposicaoRepository.findByCodigoRequisicaoIgnoreCase(c);
        if (opt.isPresent()) {
            log.info("✅ Peça encontrada por codigo_requisicao: {}", c);
            return ResponseEntity.ok(opt.get());
        }

        // ========================================
        // PRIORIDADE 4: Busca parcial por nome
        // ========================================
        List<PecaReposicao> byName = pecaReposicaoRepository.findByNomeContainingIgnoreCase(c);
        if (!byName.isEmpty()) {
            log.info("✅ {} peça(s) encontrada(s) por nome contendo: {}", byName.size(), c);
            return ResponseEntity.ok(byName);
        }

        // ========================================
        // PRIORIDADE 5: Limpeza heurística (fallback)
        // ========================================
        String cleaned = c.replaceAll("https?://[^\\s]+", "").replaceAll("[^A-Za-z0-9\\-_*]", "").trim();
        if (!cleaned.equals(c) && !cleaned.isEmpty()) {
            log.info("🧹 Tentando busca com código limpo: '{}' (original: '{}')", cleaned, c);
            
            opt = pecaReposicaoRepository.findByCodigoControleIgnoreCase(cleaned);
            if (opt.isPresent()) {
                log.info("✅ Peça encontrada por codigo_controle limpo: {}", cleaned);
                return ResponseEntity.ok(opt.get());
            }
            
            opt = pecaReposicaoRepository.findByCodigoRequisicaoIgnoreCase(cleaned);
            if (opt.isPresent()) {
                log.info("✅ Peça encontrada por codigo_requisicao limpo: {}", cleaned);
                return ResponseEntity.ok(opt.get());
            }
            
            byName = pecaReposicaoRepository.findByNomeContainingIgnoreCase(cleaned);
            if (!byName.isEmpty()) {
                log.info("✅ {} peça(s) encontrada(s) por nome limpo: {}", byName.size(), cleaned);
                return ResponseEntity.ok(byName);
            }
        }

        log.warn("❌ Peça NÃO encontrada para código: '{}'", c);
        return ResponseEntity.status(404).body(Map.of("message", "Peça não encontrada para code: " + code));
    }

    @PostMapping
    public ResponseEntity<?> createPeca(@RequestBody PecaReposicao pecaDto) {
        log.info("POST /api/pecas recebido, corpo: {}", pecaDto);

        // valor recebido (para debug)
        String recebido = pecaDto == null ? null : pecaDto.getCodigoControle();
        log.debug("Valor recebido em codigoControle: '{}'", recebido);

        if (isInvalidCodigoControle(recebido)) {
            String novo = pecaService.gerarProximoCodigoControle();
            log.info("Gerando novo codigo_controle: {} (entrada inválida: '{}')", novo, recebido);
            pecaDto.setCodigoControle(novo);
        } else {
            log.info("CodigoControle recebido válido: {}", recebido);
            // garante trimmed
            pecaDto.setCodigoControle(recebido.trim());
        }

        PecaReposicao salvo = pecaReposicaoRepository.save(pecaDto);
        log.info("Peca salva id={}, codigo_controle={}", salvo.getId(), salvo.getCodigoControle());

        // Gera e salva o QR em qr_url (não sobrescreve foto_url)
        try {
            String codigoParaQr = (salvo.getCodigoControle() != null && !salvo.getCodigoControle().isBlank())
                    ? salvo.getCodigoControle()
                    : salvo.getCodigoRequisicao(); // fallback apenas se codigo_controle inexistente
            if (codigoParaQr != null && !codigoParaQr.isBlank()) {
                String filename = codigoParaQr + ".png";
                String url = qrService.generatePngForText(codigoParaQr, filename);
                salvo.setQrUrl(url);
                pecaReposicaoRepository.save(salvo);
                log.info("QR gerado e salvo em qr_url para id={} url={}", salvo.getId(), url);
            } else {
                log.warn("Peça id={} sem código disponível para gerar QR", salvo.getId());
            }
        } catch (IOException | WriterException e) {
            log.error("Erro ao gerar QR para peca id={}: {}", salvo.getId(), e.getMessage(), e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }
}