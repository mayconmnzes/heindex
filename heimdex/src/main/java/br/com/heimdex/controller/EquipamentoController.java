package br.com.heimdex.controller;

import br.com.heimdex.dto.OrdemServicoResponseDTO;
import java.util.Collections;
import br.com.heimdex.dto.EquipamentoRequestDTO;
import br.com.heimdex.dto.EquipamentoResponseDTO;
import br.com.heimdex.model.Checklist;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.LinhaDeProducao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.model.OrdemServico;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import br.com.heimdex.service.OrdemServicoService; // ADICIONADO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    
    // CORREÇÃO: Injetando o Service em vez do Controller
    @Autowired private OrdemServicoService ordemServicoService;

    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        return equipamentoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipamento> getEquipamentoPorId(@PathVariable Long id) {
        return equipamentoRepository.findById(id)
                .map(ResponseEntity::ok)
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

                    if (dto.getChecklistId() != null) {
                         Checklist checklist = checklistRepository.findById(dto.getChecklistId()).orElse(null);
                         equipamentoExistente.setChecklistPadrao(checklist);
                     } else {
                         equipamentoExistente.setChecklistPadrao(null);
                     }

                    Equipamento updatedEquipamento = equipamentoRepository.save(equipamentoExistente);
                    return ResponseEntity.ok(convertToResponseDTO(updatedEquipamento));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEquipamento(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) { return ResponseEntity.notFound().build(); }
        try {
            equipamentoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir o equipamento.");
        }
    }

    private Equipamento convertToEntity(EquipamentoRequestDTO dto) {
        Equipamento equipamento = new Equipamento();
        equipamento.setNome(dto.getNome());
        String codigoTratado = (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) ? null : dto.getCodigo();
        equipamento.setCodigo(codigoTratado);
        equipamento.setCriticidade(dto.getCriticidade());
        equipamento.setFrequenciaPreventiva(dto.getFrequenciaPreventiva());
        equipamento.setDataUltimaPreventiva(dto.getDataUltimaPreventiva());

        LinhaDeProducao linha = linhaDeProducaoRepository.findById(dto.getLinhaId()).orElse(null);
        ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloId()).orElse(null);
        equipamento.setLinha(linha);
        equipamento.setModelo(modelo);

        return equipamento;
    }

    public EquipamentoResponseDTO convertToResponseDTO(Equipamento equipamento) {
        if (equipamento == null) return null;
        EquipamentoResponseDTO dto = new EquipamentoResponseDTO();
        dto.setId(equipamento.getId());
        dto.setNome(equipamento.getNome());
        dto.setCodigo(equipamento.getCodigo());
        dto.setCriticidade(equipamento.getCriticidade());

        if (equipamento.getModelo() != null) {
            dto.setModeloId(equipamento.getModelo().getId());
            dto.setNomeModelo(equipamento.getModelo().getNome());
            dto.setFabricante(equipamento.getModelo().getFabricante());
        }

        if (equipamento.getLinha() != null) {
            dto.setNomeLinha(equipamento.getLinha().getNome());
            if (equipamento.getLinha().getArea() != null) {
                dto.setNomeArea(equipamento.getLinha().getArea().getNome());
            }
        }
        return dto;
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<OrdemServicoResponseDTO>> getHistoricoEquipamento(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) return ResponseEntity.ok(Collections.emptyList());
        List<OrdemServico> historicoOs = ordemServicoRepository.findByEquipamentoIdOrderByDataAgendamentoDesc(id);
        
        // CORREÇÃO: Usando o Service para converter as OSs do histórico
        List<OrdemServicoResponseDTO> historicoDto = historicoOs.stream()
            .map(os -> ordemServicoService.convertToResponseDTO(os))
            .filter(d -> d != null)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(historicoDto);
    }
}
