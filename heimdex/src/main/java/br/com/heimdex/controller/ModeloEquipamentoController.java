package br.com.heimdex.controller;

import br.com.heimdex.dto.ModeloEquipamentoDTO;
import br.com.heimdex.model.Area;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.repository.AreaRepository;
import br.com.heimdex.repository.ModeloEquipamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/modelos")
public class ModeloEquipamentoController {

    @Autowired private ModeloEquipamentoRepository repository;
    @Autowired private AreaRepository areaRepository;

    @GetMapping
    public List<ModeloEquipamentoDTO> getAll() {
        return repository.findAll().stream().map(m -> {
            ModeloEquipamentoDTO dto = new ModeloEquipamentoDTO();
            dto.setId(m.getId());
            dto.setNome(m.getNome());
            dto.setFabricante(m.getFabricante());
            if (m.getArea() != null) {
                dto.setAreaId(m.getArea().getId());
                dto.setAreaNome(m.getArea().getNome());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ModeloEquipamento> create(@RequestBody ModeloEquipamento modelo) {
        if (modelo.getArea() != null) {
            Area area = areaRepository.findById(modelo.getArea().getId()).orElse(null);
            modelo.setArea(area);
        }
        return new ResponseEntity<>(repository.save(modelo), HttpStatus.CREATED);
    }
}