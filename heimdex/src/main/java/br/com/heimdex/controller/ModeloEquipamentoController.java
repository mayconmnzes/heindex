package br.com.heimdex.controller;

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

    @Autowired 
    private ModeloEquipamentoRepository repository;
    
    @Autowired 
    private AreaRepository areaRepository;

    @GetMapping
    public List<ModeloDTO> getAll() {
        List<ModeloEquipamento> modelos = repository.findAll();
        System.out.println("🔍 LOG HEIMDEX: Buscando todos os modelos. Total: " + modelos.size());
        return modelos.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ModeloDTO> create(@RequestBody ModeloEquipamento modelo) {
        System.out.println("🚀 LOG HEIMDEX: Iniciando cadastro de novo modelo: " + modelo.getNome());
        
        if (modelo.getArea() != null && modelo.getArea().getId() != null) {
            Area area = areaRepository.findById(modelo.getArea().getId())
                    .orElseThrow(() -> new RuntimeException("❌ ERRO: Área ID " + modelo.getArea().getId() + " não encontrada!"));
            modelo.setArea(area);
            System.out.println("✅ LOG HEIMDEX: Área vinculada com sucesso: " + area.getNome());
        } else {
            System.err.println("⚠️ LOG HEIMDEX: Modelo recebido sem Área vinculada!");
        }
        
        ModeloEquipamento salvo = repository.save(modelo);
        ModeloDTO dtoRetorno = convertToDTO(salvo);
        
        System.out.println("📦 LOG HEIMDEX: Modelo salvo. Retornando areaNome: " + dtoRetorno.getAreaNome());
        return new ResponseEntity<>(dtoRetorno, HttpStatus.CREATED);
    }

    private ModeloDTO convertToDTO(ModeloEquipamento m) {
        ModeloDTO dto = new ModeloDTO();
        dto.setId(m.getId());
        dto.setNome(m.getNome());
        dto.setFabricante(m.getFabricante());
        if (m.getArea() != null) {
            dto.setAreaId(m.getArea().getId());
            dto.setAreaNome(m.getArea().getNome());
        }
        return dto;
    }

    // ✅ CLASSE DTO CORRIGIDA COM GETTERS E SETTERS (Evita erro de compilação)
    public static class ModeloDTO {
        private Long id;
        private String nome;
        private String fabricante;
        private Long areaId;
        private String areaNome;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getFabricante() { return fabricante; }
        public void setFabricante(String fabricante) { this.fabricante = fabricante; }
        public Long getAreaId() { return areaId; }
        public void setAreaId(Long areaId) { this.areaId = areaId; }
        public String getAreaNome() { return areaNome; }
        public void setAreaNome(String areaNome) { this.areaNome = areaNome; }
    }
}