// Código Completo
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/modelos") // Novo endpoint
public class ModeloEquipamentoController {

    @Autowired private ModeloEquipamentoRepository modeloRepository;
    @Autowired private AreaRepository areaRepository;

    // Método auxiliar para converter Entidade em DTO
    private ModeloEquipamentoDTO convertToDto(ModeloEquipamento modelo) {
        ModeloEquipamentoDTO dto = new ModeloEquipamentoDTO();
        dto.setId(modelo.getId());
        dto.setNome(modelo.getNome());
        dto.setFabricante(modelo.getFabricante());

        if (modelo.getArea() != null) {
            dto.setAreaId(modelo.getArea().getId());
            dto.setAreaNome(modelo.getArea().getNome());
        }
        // Calcula contagens para exibição
        dto.setQuantidadeInstancias(modelo.getEquipamentos() != null ? modelo.getEquipamentos().size() : 0);
        dto.setQuantidadePecasAssociadas(modelo.getPecasAssociadas() != null ? modelo.getPecasAssociadas().size() : 0);

        return dto;
    }

    @PostMapping
    public ResponseEntity<ModeloEquipamentoDTO> criarModelo(@RequestBody ModeloEquipamentoDTO dto) {
        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Área não encontrada"));

        ModeloEquipamento novoModelo = new ModeloEquipamento();
        novoModelo.setNome(dto.getNome());
        novoModelo.setFabricante(dto.getFabricante());
        novoModelo.setArea(area);

        ModeloEquipamento modeloSalvo = modeloRepository.save(novoModelo);
        return new ResponseEntity<>(convertToDto(modeloSalvo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloEquipamentoDTO> atualizarModelo(@PathVariable Long id, @RequestBody ModeloEquipamentoDTO dto) {
        ModeloEquipamento modeloExistente = modeloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo não encontrado"));

        Area area = areaRepository.findById(dto.getAreaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Área não encontrada"));

        modeloExistente.setNome(dto.getNome());
        modeloExistente.setFabricante(dto.getFabricante());
        modeloExistente.setArea(area);

        ModeloEquipamento modeloAtualizado = modeloRepository.save(modeloExistente);
        return ResponseEntity.ok(convertToDto(modeloAtualizado));
    }

    @GetMapping
    public List<ModeloEquipamentoDTO> listarModelos() {
        return modeloRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/por-area/{areaId}")
    public List<ModeloEquipamentoDTO> listarModelosPorArea(@PathVariable Long areaId) {
        return modeloRepository.findByAreaId(areaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarModelo(@PathVariable Long id) {
        if (!modeloRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            modeloRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Captura erro se houver equipamentos ou peças associadas
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir o modelo. Verifique se ele não está associado a Equipamentos (tags) ou Peças de Reposição.");
        }
    }
}