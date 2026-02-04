package br.com.heimdex.controller;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/linhas")
public class LinhaDeProducaoController {

    @Autowired
    private LinhaDeProducaoRepository linhaRepository;

    @Autowired
    private AreaRepository areaRepository;

    @GetMapping
    public List<LinhaDeProducaoResponseDTO> getAll() {
        return linhaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<LinhaDeProducaoResponseDTO> createLinha(@RequestBody LinhaDeProducao linha) {
        // Verifica se a área foi enviada no objeto
        if (linha.getArea() == null || linha.getArea().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A Área é obrigatória para criar uma linha.");
        }

        // Busca a área real no banco para preencher o nome no retorno
        Area areaReal = areaRepository.findById(linha.getArea().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Área não encontrada no banco local."));
        
        linha.setArea(areaReal);
        LinhaDeProducao salva = linhaRepository.save(linha);

        // ✅ CORREÇÃO: Retorna o DTO em vez da Entidade
        // Isso garante que o campo 'areaNome' chegue preenchido no Frontend imediatamente
        return new ResponseEntity<>(convertToDTO(salva), HttpStatus.CREATED);
    }

    // ✅ MÉTODO DE CONVERSÃO: Centraliza a lógica para evitar N/A
    private LinhaDeProducaoResponseDTO convertToDTO(LinhaDeProducao linha) {
        LinhaDeProducaoResponseDTO dto = new LinhaDeProducaoResponseDTO();
        dto.setId(linha.getId());
        dto.setNome(linha.getNome());
        if (linha.getArea() != null) {
            dto.setAreaId(linha.getArea().getId());
            dto.setAreaNome(linha.getArea().getNome()); // Aqui o React encontra o valor
        }
        return dto;
    }

    public static class LinhaDeProducaoResponseDTO {
        private Long id;
        private String nome;
        private Long areaId;
        private String areaNome;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaNome() { return areaNome; }
        public void setAreaNome(String areaNome) { this.areaNome = areaNome; }
    }
}