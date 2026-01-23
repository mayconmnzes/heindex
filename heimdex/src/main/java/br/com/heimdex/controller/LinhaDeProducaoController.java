package br.com.heimdex.controller;

import br.com.heimdex.dto.LinhaDeProducaoResponseDTO;
import br.com.heimdex.model.Area;
import br.com.heimdex.model.LinhaDeProducao;
import br.com.heimdex.repository.AreaRepository;
import br.com.heimdex.repository.LinhaDeProducaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/linhas")
public class LinhaDeProducaoController {

    @Autowired
    private LinhaDeProducaoRepository linhaRepository;

    @Autowired
    private AreaRepository areaRepository;
    
    // MÉTODO AUXILIAR PARA CONVERTER ENTIDADE EM DTO
    private LinhaDeProducaoResponseDTO convertToDto(LinhaDeProducao linha) {
        LinhaDeProducaoResponseDTO dto = new LinhaDeProducaoResponseDTO();
        dto.setId(linha.getId());
        dto.setNome(linha.getNome());
        if (linha.getArea() != null) {
            dto.setAreaId(linha.getArea().getId());
            dto.setAreaNome(linha.getArea().getNome());
        }
        return dto;
    }

    @GetMapping
    public List<LinhaDeProducaoResponseDTO> listarLinhas() {
        return linhaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/por-area/{areaId}")
    public List<LinhaDeProducaoResponseDTO> listarLinhasPorArea(@PathVariable Long areaId) {
        return linhaRepository.findByAreaId(areaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<LinhaDeProducaoResponseDTO> criarLinha(@RequestBody Map<String, String> payload) {
        String nome = payload.get("nome");
        Long areaId = Long.parseLong(payload.get("areaId"));

        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Área não encontrada"));
        
        LinhaDeProducao novaLinha = new LinhaDeProducao();
        novaLinha.setNome(nome);
        novaLinha.setArea(area);
        
        LinhaDeProducao linhaSalva = linhaRepository.save(novaLinha);
        return new ResponseEntity<>(convertToDto(linhaSalva), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LinhaDeProducaoResponseDTO> atualizarLinha(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        LinhaDeProducao linhaExistente = linhaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha não encontrada"));
        
        String nome = payload.get("nome");
        Long areaId = Long.parseLong(payload.get("areaId"));
        
        Area area = areaRepository.findById(areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Área não encontrada"));

        linhaExistente.setNome(nome);
        linhaExistente.setArea(area);
        
        LinhaDeProducao linhaAtualizada = linhaRepository.save(linhaExistente);
        return ResponseEntity.ok(convertToDto(linhaAtualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLinha(@PathVariable Long id) {
        if (!linhaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        linhaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}