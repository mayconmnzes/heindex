package br.com.heimdex.controller;

import br.com.heimdex.dto.EquipamentoRequestDTO;
import br.com.heimdex.dto.EquipamentoResponseDTO;
import br.com.heimdex.model.Checklist;
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.LinhaDeProducao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import br.com.heimdex.service.OrdemServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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

    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        // 🔥 OTIMIZAÇÃO: busca UMA vez só todos os ids com OS preventiva ativa
        Set<Long> idsComOsAtiva;
        try {
            idsComOsAtiva = new HashSet<>(ordemServicoRepository.findEquipamentoIdsComOsAtiva(
                    "PREVENTIVA",
                    List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA)
            ));
        } catch (Throwable ignored) {
            idsComOsAtiva = Collections.emptySet();
        }

        final Set<Long> idsFinal = idsComOsAtiva;
        return equipamentoRepository.findAllWithDetails().stream()
                .map(e -> convertToResponseDTO(e, idsFinal.contains(e.getId())))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> getEquipamentoById(@PathVariable Long id) {
        return equipamentoRepository.findById(id)
                .map(e -> ResponseEntity.ok(convertToResponseDTO(e, calcStatusIndividual(e))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> createEquipamento(@RequestBody EquipamentoRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Equipamento equipamento = convertToEntity(dto);

        String codigo = (dto.getCodigo() != null && !dto.getCodigo().trim().isEmpty())
                ? dto.getCodigo().trim()
                : generateBaseCodigo(dto.getNome());

        String unique = ensureUniqueCodigo(codigo, null);
        equipamento.setCodigo(unique);

        Equipamento savedEquipamento = equipamentoRepository.save(equipamento);
        return new ResponseEntity<>(convertToResponseDTO(savedEquipamento, calcStatusIndividual(savedEquipamento)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> updateEquipamento(@PathVariable Long id, @RequestBody EquipamentoRequestDTO dto) {
        return equipamentoRepository.findById(id)
                .map(equipamentoExistente -> {
                    equipamentoExistente.setNome(dto.getNome());

                    String novoCodigo = (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) ? null : dto.getCodigo().trim();

                    if (novoCodigo == null || novoCodigo.isEmpty()) {
                        novoCodigo = generateBaseCodigo(dto.getNome());
                    }
                    String unique = ensureUniqueCodigo(novoCodigo, equipamentoExistente.getId());
                    equipamentoExistente.setCodigo(unique);

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
                        Checklist check = checklistRepository.findById(dto.getChecklistId()).orElse(null);
                        equipamentoExistente.setChecklist(check);
                    } else {
                        equipamentoExistente.setChecklist(null);
                    }

                    Equipamento updated = equipamentoRepository.save(equipamentoExistente);
                    return ResponseEntity.ok(convertToResponseDTO(updated, calcStatusIndividual(updated)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper: calcula status individualmente (usado em getById/create/update, onde é 1 só - sem problema de N+1)
    private boolean calcStatusIndividual(Equipamento e) {
        try {
            return ordemServicoRepository.existsByEquipamentoIdAndTipoManutencaoAndStatusIn(
                    e.getId(),
                    "PREVENTIVA",
                    List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA)
            );
        } catch (Throwable ignored) {
            return false;
        }
    }

    // Conversões entre Entity <-> DTO
    private Equipamento convertToEntity(EquipamentoRequestDTO dto) {
        Equipamento e = new Equipamento();
        e.setNome(dto.getNome());
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

    // 🔥 ALTERADO: recebe o status pronto, em vez de consultar o banco aqui dentro
    private EquipamentoResponseDTO convertToResponseDTO(Equipamento e, boolean temOsAtiva) {
        EquipamentoResponseDTO dto = new EquipamentoResponseDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setCodigo(e.getCodigo());
        dto.setCriticidade(e.getCriticidade());
        dto.setModeloId(e.getModelo() != null ? e.getModelo().getId() : null);
        dto.setNomeModelo(e.getModelo() != null ? e.getModelo().getNome() : null);
        dto.setFabricante(e.getModelo() != null ? e.getModelo().getFabricante() : null);
        dto.setFrequenciaPreventiva(e.getFrequenciaPreventiva());
        dto.setDataUltimaPreventiva(e.getDataUltimaPreventiva());

        // Mantém o comportamento idêntico: "AGENDADA" se houver OS ativa, senão null
        dto.setStatusPreventiva(temOsAtiva ? "AGENDADA" : null);

        dto.setNomeLinha(e.getLinha() != null ? e.getLinha().getNome() : null);
        dto.setNomeArea(e.getLinha() != null && e.getLinha().getArea() != null ? e.getLinha().getArea().getNome() : null);
        dto.setChecklistNome(e.getChecklist() != null ? e.getChecklist().getNome() : null);
        dto.setChecklistId(e.getChecklist() != null ? e.getChecklist().getId() : null);

        return dto;
    }

    // ---------- Helpers para geração/uniqueness de código ----------
    private String generateBaseCodigo(String nome) {
        if (nome == null) return "EQ";
        String base = nome.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (base.isEmpty()) base = "EQ";
        if (base.length() > 10) base = base.substring(0, 10);
        return base;
    }

    private String ensureUniqueCodigo(String candidate, Long currentEquipamentoId) {
        String tryCode = candidate;
        int suffix = 0;
        while (true) {
            boolean exists = equipamentoRepository.existsByCodigo(tryCode);
            if (exists) {
                if (currentEquipamentoId != null) {
                    Equipamento found = equipamentoRepository.findByCodigo(tryCode).orElse(null);
                    if (found != null && found.getId() != null && found.getId().equals(currentEquipamentoId)) {
                        return tryCode;
                    }
                }
                suffix++;
                tryCode = candidate + "_" + suffix;
                continue;
            }
            return tryCode;
        }
    }
}
