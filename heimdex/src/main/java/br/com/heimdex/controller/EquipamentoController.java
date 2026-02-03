package br.com.heimdex.controller;

import br.com.heimdex.dto.OrdemServicoResponseDTO;
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

    // ✅ CORREÇÃO: Apenas UMA versão do método, usando a query otimizada para performance
    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        return equipamentoRepository.findAllWithDetails().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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
        
        // ✅ MAPEAMENTO DAS DATAS: Essencial para o Planejamento.jsx
        dto.setDataUltimaPreventiva(equipamento.getDataUltimaPreventiva());
        dto.setFrequenciaPreventiva(equipamento.getFrequenciaPreventiva());

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

        if (equipamento.getChecklistPadrao() != null) {
            dto.setChecklistId(equipamento.getChecklistPadrao().getId());
            dto.setChecklistNome(equipamento.getChecklistPadrao().getNome());
        }

        // 🔥 LÓGICA DO STATUS: Alimenta o gráfico e cores dos cards no Frontend
        dto.setStatusPreventiva(calcularStatus(equipamento));

        return dto;
    }

    private String calcularStatus(Equipamento e) {
        // 1. Verifica se existe alguma OS de PREVENTIVA vinculada a este equipamento
        // Se houver qualquer registro no histórico, consideramos "AGENDADA" para simplificar o gráfico agora
        List<OrdemServico> ordens = ordemServicoRepository.findByEquipamentoIdOrderByDataAgendamentoDesc(e.getId());
        
        if (!ordens.isEmpty()) {
            // Se a última OS ainda não tiver data de fim, ela está "em aberto"
            if (ordens.get(0).getDataFimExecucao() == null) {
                return "AGENDADA";
            }
        }

        if (e.getDataUltimaPreventiva() == null || e.getFrequenciaPreventiva() == null) {
            return "NAO_CONFIGURADA";
        }

        // 2. Calcula a data de vencimento
        LocalDate vencimento = e.getDataUltimaPreventiva();
        switch (e.getFrequenciaPreventiva()) {
            case QUINZENAL -> vencimento = vencimento.plusDays(15);
            case MENSAL -> vencimento = vencimento.plusMonths(1);
            case TRIMESTRAL -> vencimento = vencimento.plusMonths(3);
            case SEMESTRAL -> vencimento = vencimento.plusMonths(6);
            case ANUAL -> vencimento = vencimento.plusYears(1);
        }

        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(vencimento)) return "ATRASADA";
        if (hoje.plusDays(7).isAfter(vencimento)) return "ATENCAO";

        return "OK";
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<OrdemServicoResponseDTO>> getHistoricoEquipamento(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) return ResponseEntity.ok(Collections.emptyList());
        List<OrdemServico> historicoOs = ordemServicoRepository.findByEquipamentoIdOrderByDataAgendamentoDesc(id);
        
        List<OrdemServicoResponseDTO> historicoDto = historicoOs.stream()
            .map(os -> ordemServicoService.convertToResponseDTO(os))
            .filter(d -> d != null)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(historicoDto);
    }
}