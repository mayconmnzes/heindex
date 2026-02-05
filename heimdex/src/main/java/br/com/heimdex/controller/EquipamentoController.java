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

    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        return equipamentoRepository.findAllWithDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> getEquipamentoById(@PathVariable Long id) {
        return equipamentoRepository.findById(id)
                .map(e -> ResponseEntity.ok(convertToResponseDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponseDTO> createEquipamento(@RequestBody EquipamentoRequestDTO dto) {
        // Validação mínima
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Equipamento equipamento = convertToEntity(dto);

        // Gera código se não informado e garante unicidade
        String codigo = (dto.getCodigo() != null && !dto.getCodigo().trim().isEmpty())
                ? dto.getCodigo().trim()
                : generateBaseCodigo(dto.getNome());

        String unique = ensureUniqueCodigo(codigo, null);
        equipamento.setCodigo(unique);

        Equipamento savedEquipamento = equipamentoRepository.save(equipamento);
        return new ResponseEntity<>(convertToResponseDTO(savedEquipamento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipamentoResponseDTO> updateEquipamento(@PathVariable Long id, @RequestBody EquipamentoRequestDTO dto) {
        return equipamentoRepository.findById(id)
                .map(equipamentoExistente -> {
                    equipamentoExistente.setNome(dto.getNome());

                    String novoCodigo = (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) ? null : dto.getCodigo().trim();

                    // Se não enviou novo codigo, gera a partir do nome; garante unicidade (permitindo manter o código atual)
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
                    return ResponseEntity.ok(convertToResponseDTO(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Conversões entre Entity <-> DTO
    private Equipamento convertToEntity(EquipamentoRequestDTO dto) {
        Equipamento e = new Equipamento();
        e.setNome(dto.getNome());
        // código será aplicado no create/update para garantir unicidade e não-null
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
        dto.setModeloId(e.getModelo() != null ? e.getModelo().getId() : null);
        dto.setNomeModelo(e.getModelo() != null ? e.getModelo().getNome() : null);
        dto.setFabricante(e.getModelo() != null ? e.getModelo().getFabricante() : null);
        dto.setFrequenciaPreventiva(e.getFrequenciaPreventiva());
        dto.setDataUltimaPreventiva(e.getDataUltimaPreventiva());

        // Decide statusPreventiva: se houver uma PREVENTIVA AGENDADA ou SUGESTAO para este equipamento,
        // marca como AGENDADA para que o frontend trate como "programado".
        try {
            boolean hasAgendadaOrSugestao = ordemServicoRepository.existsByEquipamentoIdAndTipoManutencaoAndStatusIn(
                    e.getId(),
                    "PREVENTIVA",
                    List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA)
            );
            if (hasAgendadaOrSugestao) {
                dto.setStatusPreventiva("AGENDADA");
            } else {
                dto.setStatusPreventiva(null);
            }
        } catch (Throwable ignored) {
            dto.setStatusPreventiva(null);
        }

        dto.setNomeLinha(e.getLinha() != null ? e.getLinha().getNome() : null);
        dto.setNomeArea(e.getLinha() != null && e.getLinha().getArea() != null ? e.getLinha().getArea().getNome() : null);
        dto.setChecklistNome(e.getChecklist() != null ? e.getChecklist().getNome() : null);
        dto.setChecklistId(e.getChecklist() != null ? e.getChecklist().getId() : null);

        return dto;
    }

    // ---------- Helpers para geração/uniqueness de código ----------
    private String generateBaseCodigo(String nome) {
        if (nome == null) return "EQ";
        // Remove caracteres não alfanuméricos e deixa em maiúsculas
        String base = nome.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (base.isEmpty()) base = "EQ";
        if (base.length() > 10) base = base.substring(0, 10);
        return base;
    }

    /**
     * Garante unicidade do código. Se já existir, acrescenta sufixo _1, _2, ...
     * @param candidate código base
     * @param currentEquipamentoId quando for atualização, id do equipamento atual (para permitir manter o mesmo código)
     * @return código único
     */
    private String ensureUniqueCodigo(String candidate, Long currentEquipamentoId) {
        String tryCode = candidate;
        int suffix = 0;
        while (true) {
            boolean exists = equipamentoRepository.existsByCodigo(tryCode);
            // Se existe e é o próprio equipamento (no update), aceita
            if (exists) {
                if (currentEquipamentoId != null) {
                    // permite se o equipamento com esse código for o mesmo que estamos atualizando
                    Equipamento found = equipamentoRepository.findByCodigo(tryCode).orElse(null);
                    if (found != null && found.getId() != null && found.getId().equals(currentEquipamentoId)) {
                        return tryCode;
                    }
                }
                // senão tenta próximo sufixo
                suffix++;
                tryCode = candidate + "_" + suffix;
                continue;
            }
            // não existe -> único
            return tryCode;
        }
    }
}