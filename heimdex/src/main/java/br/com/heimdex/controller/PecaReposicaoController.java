package br.com.heimdex.controller;

import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.dto.MovimentacaoEstoqueResponseDTO;
import br.com.heimdex.model.MovimentacaoEstoque;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.repository.MovimentacaoEstoqueRepository;
import br.com.heimdex.repository.PecaReposicaoRepository;
import br.com.heimdex.repository.ModeloEquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;

    @GetMapping
    public List<PecaReposicaoResponseDTO> getAll() {
        // ✅ Performance máxima: Carrega tudo em 1 só query
        return pecaRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<PecaReposicaoResponseDTO> create(
            @RequestPart("peca") PecaReposicao pecaBody, 
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo
    ) {
        // ✅ VÍNCULO AUTOMÁTICO: Usa o ID temporário vindo do Front para buscar o Modelo real
        if (pecaBody.getModeloEquipamentoId() != null) {
            ModeloEquipamento modelo = modeloRepository.findById(pecaBody.getModeloEquipamentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo não encontrado"));
            pecaBody.setModeloEquipamento(modelo);
        }

        // Aqui você integraria a lógica de upload do 'arquivo' se necessário
        
        PecaReposicao saved = pecaRepository.save(pecaBody);
        return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<Void> getQrCode(@PathVariable Long id) {
        PecaReposicao peca = pecaRepository.findById(id).orElse(null);
        String conteudo = (peca != null && peca.getCodigoControle() != null) 
                          ? peca.getCodigoControle() : "ID-" + id;
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=" + conteudo))
                .build();
    }

    private PecaReposicaoResponseDTO convertToDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setCodigoControle(peca.getCodigoControle());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        dto.setLocalizacaoPrateleira(peca.getLocalizacaoPrateleira());
        dto.setCodigoRequisicao(peca.getCodigoRequisicao());
        dto.setDescricaoTecnica(peca.getDescricaoTecnica());
        dto.setAplicacao(peca.getAplicacao());
        dto.setFotoUrl(peca.getFotoUrl());

        if (peca.getModeloEquipamento() != null) {
            dto.setModeloEquipamentoId(peca.getModeloEquipamento().getId());
            dto.setNomeModeloEquipamento(peca.getModeloEquipamento().getNome());
            dto.setFabricanteModeloEquipamento(peca.getModeloEquipamento().getFabricante());
            
            if (peca.getModeloEquipamento().getArea() != null) {
                dto.setNomeArea(peca.getModeloEquipamento().getArea().getNome());
            }
        }
        return dto;
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> getHistorico(@PathVariable Long id) {
        List<MovimentacaoEstoque> movs = movimentacaoRepository.findByPecaIdOrderByDataHoraDesc(id);
        return ResponseEntity.ok(movs.stream().map(m -> {
            MovimentacaoEstoqueResponseDTO d = new MovimentacaoEstoqueResponseDTO();
            d.setId(m.getId());
            d.setQuantidade(m.getQuantidade());
            d.setTipo(m.getTipo());
            d.setDataHora(m.getDataHora());
            return d;
        }).collect(Collectors.toList()));
    }
}