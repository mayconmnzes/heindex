package br.com.heimdex.controller;

import br.com.heimdex.dto.PecaReposicaoResponseDTO;
import br.com.heimdex.dto.MovimentacaoEstoqueResponseDTO;
import br.com.heimdex.model.MovimentacaoEstoque;
import br.com.heimdex.model.PecaReposicao;
import br.com.heimdex.repository.MovimentacaoEstoqueRepository;
import br.com.heimdex.repository.PecaReposicaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pecas")
public class PecaReposicaoController {

    @Autowired
    private PecaReposicaoRepository pecaRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @GetMapping
    public List<PecaReposicaoResponseDTO> getAll() {
        return pecaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PecaReposicaoResponseDTO convertToDTO(PecaReposicao peca) {
        PecaReposicaoResponseDTO dto = new PecaReposicaoResponseDTO();
        dto.setId(peca.getId());
        dto.setNome(peca.getNome());
        dto.setCodigoControle(peca.getCodigoControle());
        dto.setEstoqueAtual(peca.getEstoqueAtual());
        dto.setEstoqueMinimo(peca.getEstoqueMinimo());
        dto.setLocalizacaoPrateleira(peca.getLocalizacaoPrateleira());
        return dto;
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<MovimentacaoEstoqueResponseDTO>> getHistorico(@PathVariable Long id) {
        List<MovimentacaoEstoque> movs = movimentacaoRepository.findByPecaIdOrderByDataHoraDesc(id);
        List<MovimentacaoEstoqueResponseDTO> dtos = movs.stream().map(mov -> {
            MovimentacaoEstoqueResponseDTO dto = new MovimentacaoEstoqueResponseDTO();
            dto.setId(mov.getId());
            dto.setQuantidade(mov.getQuantidade());
            dto.setTipo(mov.getTipo());
            dto.setDataHora(mov.getDataHora());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
