package br.com.heimdex.controller;

import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.repository.ModeloEquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pecas")
public class PecaReposicaoController {

    @Autowired private PecaReposicaoRepository pecaRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;

    @GetMapping
    public List<PecaReposicaoResponseDTO> getAll() {
        return pecaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PecaReposicaoResponseDTO> create(
            @RequestPart("peca") PecaReposicao pecaBody, 
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo
    ) {
        try {
            // 1. VÍNCULO MÚLTIPLO: Busca todos os modelos enviados pelo Front
            if (pecaBody.getModelosIds() != null && !pecaBody.getModelosIds().isEmpty()) {
                List<ModeloEquipamento> modelos = modeloRepository.findAllById(pecaBody.getModelosIds());
                pecaBody.setModelosEquipamentos(modelos);
            }

            // 2. TRATA A FOTO (Exemplo: salvando a URL gerada)
            if (arquivo != null && !arquivo.isEmpty()) {
                pecaBody.setFotoUrl("/uploads/" + arquivo.getOriginalFilename());
            }
            
            PecaReposicao saved = pecaRepository.save(pecaBody);
            return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao processar peça: " + e.getMessage());
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
        
        // ✅ NOVA URL: Google Charts antigo (/chart) saiu do ar. Usamos o QRServer que é estável.
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + conteudo;
        
        return ResponseEntity.ok(qrUrl);
    }

    private PecaReposicaoResponseDTO convertToDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        dto.setFotoUrl(peca.getFotoUrl());
        dto.setCodigoRequisicao(peca.getCodigoRequisicao());

        // ✅ MAPEIA LISTA PARA TEXTO: Transforma [Máquina A, Máquina B] em string para a tabela
        if (peca.getModelosEquipamentos() != null && !peca.getModelosEquipamentos().isEmpty()) {
            String maquinas = peca.getModelosEquipamentos().stream()
                    .map(ModeloEquipamento::getNome).collect(Collectors.joining(", "));
            String areas = peca.getModelosEquipamentos().stream()
                    .map(m -> m.getArea() != null ? m.getArea().getNome() : "N/A")
                    .distinct().collect(Collectors.joining(", "));
            
            dto.setNomeModeloEquipamento(maquinas);
            dto.setNomeArea(areas);
        }
        return dto;
    }
}