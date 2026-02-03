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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/linhas")
public class LinhaDeProducaoController {

    @Autowired private LinhaDeProducaoRepository repository;
    @Autowired private AreaRepository areaRepository;

    @GetMapping
    public List<LinhaDeProducaoResponseDTO> getAllLinhas() {
        return repository.findAll().stream().map(linha -> {
            LinhaDeProducaoResponseDTO dto = new LinhaDeProducaoResponseDTO();
            dto.setId(linha.getId());
            dto.setNome(linha.getNome());
            if (linha.getArea() != null) {
                dto.setAreaId(linha.getArea().getId());
                dto.setAreaNome(linha.getArea().getNome());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<LinhaDeProducao> createLinha(@RequestBody LinhaDeProducao request) {
        Area area = areaRepository.findById(request.getArea().getId())
                .orElseThrow(() -> new RuntimeException("Área não encontrada"));
        LinhaDeProducao nova = new LinhaDeProducao();
        nova.setNome(request.getNome());
        nova.setArea(area);
        return new ResponseEntity<>(repository.save(nova), HttpStatus.CREATED);
    }
}