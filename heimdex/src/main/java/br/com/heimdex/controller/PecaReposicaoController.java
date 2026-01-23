package br.com.heimdex.controller;

import br.com.heimdex.dto.MovimentacaoEstoqueDTO;
import br.com.heimdex.dto.PecaReposicaoRequestDTO;
import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.model.*;
import br.com.heimdex.repository.*;
import br.com.heimdex.service.CloudinaryService; 
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pecas")
public class PecaReposicaoController {

    @Autowired private PecaReposicaoRepository pecaReposicaoRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;
    
    @Autowired private CloudinaryService cloudinaryService;

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode(@PathVariable("id") Long id) {
        Optional<PecaReposicao> pecaOpt = pecaReposicaoRepository.findById(id);
        if (pecaOpt.isEmpty() || pecaOpt.get().getCodigoControle() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            String qrCodeText = pecaOpt.get().getCodigoControle();
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return ResponseEntity.ok(pngOutputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<PecaReposicaoResponseDTO> criarPeca(
            @RequestPart("peca") PecaReposicaoRequestDTO dto,
            @RequestPart(value = "arquivo", required = false) MultipartFile arquivo) throws IOException {
        
        PecaReposicao novaPeca = new PecaReposicao();
        novaPeca.setNome(dto.getNome());
        novaPeca.setCodigoRequisicao(dto.getCodigoRequisicao());
        novaPeca.setDescricaoGenerica(dto.getDescricaoGenerica());
        novaPeca.setDescricaoTecnica(dto.getDescricaoTecnica());
        novaPeca.setAplicacao(dto.getAplicacao());
        novaPeca.setLocalizacaoPrateleira(dto.getLocalizacaoPrateleira());
        novaPeca.setEstoqueAtual(dto.getEstoqueAtual());
        novaPeca.setEstoqueMinimo(dto.getEstoqueMinimo());

        if (arquivo != null && !arquivo.isEmpty()) {
            String urlNuvem = cloudinaryService.uploadImage(arquivo);
            novaPeca.setFotoUrl(urlNuvem);
        }

        if (dto.getModeloEquipamentoId() != null) {
            ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloEquipamentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo não encontrado."));
            novaPeca.setModeloEquipamento(modelo);
        }

        // ✅ CORREÇÃO: Comentado para evitar que o Hibernate procure a tabela inexistente 'peca_modelos_ids'
        // novaPeca.setModelosIds(dto.getModelosIds());
        
        PecaReposicao pecaSalva = pecaReposicaoRepository.save(novaPeca);
        pecaSalva.setCodigoControle("PECA-" + String.format("%05d", pecaSalva.getId()));
        return new ResponseEntity<>(convertToResponseDTO(pecaReposicaoRepository.save(pecaSalva)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<PecaReposicaoResponseDTO> updatePeca(@PathVariable Long id, @RequestBody PecaReposicaoRequestDTO dto) {
        return pecaReposicaoRepository.findById(id).map(pecaExistente -> {
            pecaExistente.setNome(dto.getNome());
            pecaExistente.setCodigoRequisicao(dto.getCodigoRequisicao());
            pecaExistente.setDescricaoGenerica(dto.getDescricaoGenerica());
            pecaExistente.setDescricaoTecnica(dto.getDescricaoTecnica());
            pecaExistente.setAplicacao(dto.getAplicacao());
            pecaExistente.setLocalizacaoPrateleira(dto.getLocalizacaoPrateleira());
            pecaExistente.setEstoqueAtual(dto.getEstoqueAtual());
            pecaExistente.setEstoqueMinimo(dto.getEstoqueMinimo());
            
            if (dto.getModeloEquipamentoId() != null) {
                ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloEquipamentoId()).orElse(null);
                pecaExistente.setModeloEquipamento(modelo);
            }

            // ✅ CORREÇÃO: Comentado para não travar no 'update'
            // pecaExistente.setModelosIds(dto.getModelosIds());
            return ResponseEntity.ok(convertToResponseDTO(pecaReposicaoRepository.save(pecaExistente)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<PecaReposicaoResponseDTO> listarPecas() {
        return pecaReposicaoRepository.findAll().stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
    }

    @PostMapping("/{id}/entrada")
    @Transactional
    public ResponseEntity<?> entradaEstoque(@PathVariable Long id, @RequestBody MovimentacaoEstoqueDTO dto) {
        PecaReposicao peca = pecaReposicaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada."));
        peca.setEstoqueAtual(peca.getEstoqueAtual() + dto.getQuantidade());
        return ResponseEntity.ok(convertToResponseDTO(pecaReposicaoRepository.save(peca)));
    }

    @PostMapping("/{id}/saida")
    @Transactional
    public ResponseEntity<?> saidaEstoque(@PathVariable Long id, @RequestBody MovimentacaoEstoqueDTO dto) {
        PecaReposicao peca = pecaReposicaoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça não encontrada."));
        if (peca.getEstoqueAtual() < dto.getQuantidade()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estoque insuficiente.");
        }
        peca.setEstoqueAtual(peca.getEstoqueAtual() - dto.getQuantidade());
        return ResponseEntity.ok(convertToResponseDTO(pecaReposicaoRepository.save(peca)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletePeca(@PathVariable Long id) {
        if (!pecaReposicaoRepository.existsById(id)) return ResponseEntity.notFound().build();
        pecaReposicaoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private PecaReposicaoResponseDTO convertToResponseDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setCodigoControle(peca.getCodigoControle());
        dto.setNome(peca.getNome());
        dto.setCodigoRequisicao(peca.getCodigoRequisicao());
        dto.setDescricaoGenerica(peca.getDescricaoGenerica());
        dto.setDescricaoTecnica(peca.getDescricaoTecnica());
        dto.setAplicacao(peca.getAplicacao());
        dto.setLocalizacaoPrateleira(peca.getLocalizacaoPrateleira());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        
        // ✅ Link do Cloudinary (dkreomuo2)
        dto.setFotoUrl(peca.getFotoUrl()); 

        if (peca.getModeloEquipamento() != null) {
            dto.setModeloEquipamentoId(peca.getModeloEquipamento().getId());
            dto.setNomeModeloEquipamento(peca.getModeloEquipamento().getNome());
            dto.setFabricanteModeloEquipamento(peca.getModeloEquipamento().getFabricante());
        }
        
        // ✅ CORREÇÃO FINAL: Removida a chamada ao getModelosIds() que causava o erro 500
        // dto.setModelosIds(peca.getModelosIds());
        return dto;
    }
}