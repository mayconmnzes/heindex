package br.com.heimdex.controller;

import br.com.heimdex.model.Area;
import br.com.heimdex.repository.AreaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/areas")
public class AreaController {

    @Autowired
    private AreaRepository areaRepository;

    @GetMapping
    public List<AreaDTO> listarAreas() {
        return areaRepository.findAll().stream().map(area -> {
            AreaDTO dto = new AreaDTO();
            dto.setId(area.getId());
            dto.setNome(area.getNome());
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Area> criarArea(@RequestBody Area area) {
        return new ResponseEntity<>(areaRepository.save(area), HttpStatus.CREATED);
    }

    // DTO interno para evitar loop infinito
    public static class AreaDTO {
        private Long id;
        private String nome;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String n) { this.nome = n; }
    }
}