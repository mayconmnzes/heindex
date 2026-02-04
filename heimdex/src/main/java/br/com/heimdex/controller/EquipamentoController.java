package br.com.heimdex.controller;

import br.com.heimdex.dto.EquipamentoRequestDTO;
import br.com.heimdex.dto.EquipamentoResponseDTO;
import br.com.heimdex.model.Checklist;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.LinhaDeProducao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import br.com.heimdex.service.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private LinhaDeProducaoRepository linhaDeProducaoRepository;
    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private OrdemServicoRepository ordemServicoRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;
    @Autowired private OrdemServicoService ordemServicoService;

    // Lista todos (com detalhes) - já existente
    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        return equipamentoRepository.findAllWithDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Novo endpoint: obter equipamento por id (inclui dataUltimaPreventiva etc.)
    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> getEquipamentoById(@PathVariable Long id) {
        return equipamentoRepository.findById(id)
                .map(e -> ResponseEntity.ok(convertToResponseDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> createEquipamento(@RequestBody EquipamentoRequestDTO dto) {
        Equipamento equipamento = convertToEntity(dto);
        Equipamento savedEquipamento = equipamentoRepository.save(equipamento);
        return new ResponseEntity<>(convertToResponseDTO(savedEquipamento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> updateEquipamento(@PathVariable Long id, @RequestBody EquipamentoRequestDTO dto) {
        return equipamentoRepository.findById(id)
                .map(equipamentoExistente -> {
                    equipamentoExistente.setNome(dto.getNome());
                    String novoCodigo = (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) ? null : dto.getCodigo();
                    equipamentoExistente.setCodigo(novoCodigo);

                    equipamentoExistente.setCriticidade(dto.getCriticidade());
                    equipamentoExistente.setFrequenciaPreventiva(dto.getFrequenciaPreventiva());
                    equipamentoExistente.setDataUltimaPreventiva(dto.getDataUltimaPreventiva());

                    LinhaDeProducao linha = linhaDeProducaoRepository.findById(dto.getLinhaId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha de Produção não encontrada"));
                    ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de Equipamento não encontrado"));

                    equipamentoExistente.setLinha(linha);
                    equipamentoExistente.setModelo(modelo);

                    // Corrigido para usar setChecklist (igual ao Model)
                    if (dto.getChecklistId() != null) {
                        Checklist check = checklistRepository.findById(dto.getChecklistId()).orElse(null);
                        equipamentoExistente.setChecklist(check);
                    } else {
                        equipamentoExistente.setChecklist(null);
                    }

                    Equipamento updated = equipamentoRepository.save(equipamentoExistente);
                    return ResponseEntity.ok(convertToResponseDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Convert e convertToResponse (mantidos/ajustados)
    private Equipamento convertToEntity(EquipamentoRequestDTO dto) {
        Equipamento e = new Equipamento();
        e.setNome(dto.getNome());
        e.setCodigo(dto.getCodigo());
        e.setCriticidade(dto.getCriticidade());
        e.setFrequenciaPreventiva(dto.getFrequenciaPreventiva());
        e.setDataUltimaPreventiva(dto.getDataUltimaPreventiva());

        LinhaDeProducao linha = linhaDeProducaoRepository.findById(dto.getLinhaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha de Produção não encontrada"));
        ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de Equipamento não encontrado"));

        e.setLinha(linha);
        e.setModelo(modelo);

        if (dto.getChecklistId() != null) {
            Checklist check = checklistRepository.findById(dto.getChecklistId()).orElse(null);
            e.setChecklist(check);
        }
        return e;
    }

    private EquipamentoResponseDTO convertToResponseDTO(Equipamento e) {
        EquipamentoResponseDTO dto = new EquipamentoResponseDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setCodigo(e.getCodigo());
        dto.setCriticidade(e.getCriticidade());
        dto.setFrequenciaPreventiva(e.getFrequenciaPreventiva());
        dto.setDataUltimaPreventiva(e.getDataUltimaPreventiva());
        dto.setLinhaId(e.getLinha() != null ? e.getLinha().getId() : null);
        dto.setModeloId(e.getModelo() != null ? e.getModelo().getId() : null);
        dto.setChecklistId(e.getChecklist() != null ? e.getChecklist().getId() : null);
        // outros campos conforme seu DTO
        return dto;
    }
}