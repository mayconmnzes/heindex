package br.com.heimdex.controller;

import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.service.PecaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private static final Logger log = LoggerFactory.getLogger(PecaController.class);

    @Autowired
    private PecaReposicaoRepository pecaReposicaoRepository;

    @Autowired
    private PecaService pecaService;

    @GetMapping("/lookup")
    public ResponseEntity<?> lookupPeca(@RequestParam("code") String code) {
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Parâmetro 'code' obrigatório");
        }
        String c = code.trim();

        // 1) busca exata por codigo_controle
        Optional<PecaReposicao> opt = pecaReposicaoRepository.findByCodigoControleIgnoreCase(c);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());

        // 2) busca exata por codigo_requisicao
        opt = pecaReposicaoRepository.findByCodigoRequisicaoIgnoreCase(c);
        if (opt.isPresent()) return ResponseEntity.ok(opt.get());

        // 3) busca por nome contendo
        List<PecaReposicao> byName = pecaReposicaoRepository.findByNomeContainingIgnoreCase(c);
        if (!byName.isEmpty()) return ResponseEntity.ok(byName);

        // 4) tentativa heurística: limpar prefixos/sufixos comuns (ex: 'QR:' 'http://...' etc)
        String cleaned = c.replaceAll("https?://[^\\s]+", "").replaceAll("[^A-Za-z0-9\\-_*]", "").trim();
        if (!cleaned.equals(c)) {
            opt = pecaReposicaoRepository.findByCodigoControleIgnoreCase(cleaned);
            if (opt.isPresent()) return ResponseEntity.ok(opt.get());
            opt = pecaReposicaoRepository.findByCodigoRequisicaoIgnoreCase(cleaned);
            if (opt.isPresent()) return ResponseEntity.ok(opt.get());
            byName = pecaReposicaoRepository.findByNomeContainingIgnoreCase(cleaned);
            if (!byName.isEmpty()) return ResponseEntity.ok(byName);
        }

        return ResponseEntity.status(404).body(Map.of("message", "Peça não encontrada para code: " + code));
    }

    @PostMapping
    public ResponseEntity<?> createPeca(@RequestBody PecaReposicao pecaDto) {
        log.info("POST /api/pecas recebido, corpo: {}", pecaDto);
        if (pecaDto.getCodigoControle() == null || pecaDto.getCodigoControle().trim().isEmpty()) {
            String novo = pecaService.gerarProximoCodigoControle();
            log.info("Gerando novo codigo_controle: {}", novo);
            pecaDto.setCodigoControle(novo);
        } else {
            log.info("CodigoControle recebido: {}", pecaDto.getCodigoControle());
        }
        PecaReposicao salvo = pecaReposicaoRepository.save(pecaDto);
        log.info("Peca salva id={}, codigo_controle={}", salvo.getId(), salvo.getCodigoControle());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }
}