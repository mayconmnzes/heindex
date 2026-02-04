package br.com.heimdex.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.heimdex.dto.MovimentacaoEstoqueDTO;
import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.model.MovimentacaoEstoque;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.repository.ModeloEquipamentoRepository;
import br.com.heimdex.repository.MovimentacaoEstoqueRepository;
import br.com.heimdex.repository.EquipamentoRepository;
import br.com.heimdex.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping({"/api/pecas", "/api/estoque"}) // ✅ Resolve erro 500 no Front
public class PecaReposicaoController {

    @Autowired private PecaReposicaoRepository pecaRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<PecaReposicaoResponseDTO> getAll() {
        return pecaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/consumo-os/{osId}")
    public ResponseEntity<?> getConsumoPorOS(@PathVariable Long osId) {
        try {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/historico/geral")
    public List<MovimentacaoEstoqueDTO> getHistoricoGeral() {
        return movimentacaoRepository.findAllByOrderByDataMovimentacaoDesc()
                .stream()
                .map(mov -> {
                    MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
                    dto.setId(mov.getId());
                    dto.setPecaId(mov.getPeca() != null ? mov.getPeca().getId() : null);
                    dto.setQuantidade(mov.getQuantidade());
                    dto.setTipoMovimentacao(mov.getTipoMovimentacao());
                    dto.setDataMovimentacao(mov.getDataMovimentacao());
                    dto.setNomePeca(mov.getPeca() != null ? mov.getPeca().getNome() : "N/A");
                    dto.setNomeEquipamento(mov.getEquipamento() != null ? mov.getEquipamento().getNome() : "Geral");
                    dto.setLoginUsuario(mov.getUsuario() != null ? mov.getUsuario().getLogin() : "Sistema");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/historico")
    public List<MovimentacaoEstoqueDTO> getHistorico(@PathVariable Long id) {
        return movimentacaoRepository.findByPecaIdOrderByDataMovimentacaoDesc(id)
                .stream()
                .map(mov -> {
                    MovimentacaoEstoqueDTO dto = new MovimentacaoEstoqueDTO();
                    dto.setId(mov.getId());
                    dto.setPecaId(mov.getPeca() != null ? mov.getPeca().getId() : null);
                    dto.setQuantidade(mov.getQuantidade());
                    dto.setTipoMovimentacao(mov.getTipoMovimentacao());
                    dto.setDataMovimentacao(mov.getDataMovimentacao());
                    dto.setNomePeca(mov.getPeca() != null ? mov.getPeca().getNome() : "N/A");
                    dto.setNomeEquipamento(mov.getEquipamento() != null ? mov.getEquipamento().getNome() : "Almoxarifado");
                    dto.setLoginUsuario(mov.getUsuario() != null ? mov.getUsuario().getLogin() : "Sistema");
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/saida")
    @Transactional 
    public ResponseEntity<?> registrarSaida(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            PecaReposicao peca = pecaRepository.findById(id).orElseThrow(() -> new RuntimeException("Não encontrada"));
            Integer qtd = Integer.parseInt(payload.get("quantidade").toString());
            Long equipId = payload.get("equipamentoId") != null ? Long.parseLong(payload.get("equipamentoId").toString()) : null;
            Long userLogId = payload.get("usuarioId") != null ? Long.parseLong(payload.get("usuarioId").toString()) : null;

            if (peca.getEstoqueAtual() < qtd) return ResponseEntity.badRequest().body("Estoque insuficiente!");

            peca.setEstoqueAtual(peca.getEstoqueAtual() - qtd);
            pecaRepository.save(peca);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setPeca(peca);
            mov.setQuantidade(qtd);
            mov.setTipoMovimentacao("SAIDA");
            mov.setDataMovimentacao(java.time.LocalDateTime.now());
            if (equipId != null) mov.setEquipamento(equipamentoRepository.findById(equipId).orElse(null));
            if (userLogId != null) mov.setUsuario(usuarioRepository.findById(userLogId).orElse(null));

            movimentacaoRepository.save(mov);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/entrada")
    @Transactional
    public ResponseEntity<?> registrarEntrada(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            PecaReposicao peca = pecaRepository.findById(id).orElseThrow(() -> new RuntimeException("Não encontrada"));
            Integer qtd = Integer.parseInt(payload.get("quantidade").toString());
            Long userLogId = payload.get("usuarioId") != null ? Long.parseLong(payload.get("usuarioId").toString()) : null;

            peca.setEstoqueAtual(peca.getEstoqueAtual() + qtd);
            pecaRepository.save(peca);

            MovimentacaoEstoque mov = new MovimentacaoEstoque();
            mov.setPeca(peca);
            mov.setQuantidade(qtd);
            mov.setTipoMovimentacao("ENTRADA");
            mov.setDataMovimentacao(java.time.LocalDateTime.now());
            if (userLogId != null) mov.setUsuario(usuarioRepository.findById(userLogId).orElse(null));

            movimentacaoRepository.save(mov);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PecaReposicaoResponseDTO> create(
            @RequestPart("peca") PecaReposicao pecaBody, 
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo
    ) {
        try {
            if (arquivo != null && !arquivo.isEmpty()) {
                String nomeArquivo = arquivo.getOriginalFilename();
                Path uploadPath = Paths.get("./uploads");
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(nomeArquivo);
                Files.copy(arquivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                pecaBody.setFotoUrl(nomeArquivo);
            }
            if (pecaBody.getModelosIds() != null && !pecaBody.getModelosIds().isEmpty()) {
                List<ModeloEquipamento> modelos = modeloRepository.findAllById(pecaBody.getModelosIds());
                // ✅ CORREÇÃO APLICADA: 'setModelosEquipamentos' é o método correto
                pecaBody.setModelosEquipamentos(modelos);
            }
            PecaReposicao saved = pecaRepository.save(pecaBody);
            return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!pecaRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        pecaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<String> getQrCode(@PathVariable Long id) {
        PecaReposicao peca = pecaRepository.findById(id).orElse(null);
        String conteudo = (peca != null && peca.getCodigoRequisicao() != null && !peca.getCodigoRequisicao().isEmpty()) 
                          ? peca.getCodigoRequisicao() : "ID-" + id;
        return ResponseEntity.ok("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + conteudo);
    }

    private PecaReposicaoResponseDTO convertToDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        dto.setFotoUrl(peca.getFotoUrl());
        dto.setCodigoRequisicao(peca.getCodigoRequisicao());
        if (peca.getModelosEquipamentos() != null && !peca.getModelosEquipamentos().isEmpty()) {
            dto.setNomeModeloEquipamento(peca.getModelosEquipamentos().stream().map(ModeloEquipamento::getNome).collect(Collectors.joining(", ")));
            dto.setNomeArea(peca.getModelosEquipamentos().stream().map(m -> m.getArea() != null ? m.getArea().getNome() : "N/A").distinct().collect(Collectors.joining(", ")));
        }
        return dto;
    }
}