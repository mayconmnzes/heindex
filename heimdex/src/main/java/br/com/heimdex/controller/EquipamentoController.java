// Código Completo Corrigido e com Adições - EquipamentoController.java
package br.com.heimdex.controller;

// --- Adicionados Imports para Histórico ---
import br.com.heimdex.dto.OrdemServicoResponseDTO;
import java.util.Collections;
// --- Fim Adições ---

import br.com.heimdex.dto.EquipamentoRequestDTO;
import br.com.heimdex.dto.EquipamentoResponseDTO;
import br.com.heimdex.model.Checklist; // Import necessário
import br.com.heimdex.model.Equipamento;
import br.com.heimdex.model.LinhaDeProducao;
import br.com.heimdex.model.ModeloEquipamento;
import br.com.heimdex.model.OrdemServico; // Import OrdemServico
import br.com.heimdex.model.enums.StatusOrdemServico;
import br.com.heimdex.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {

    @Autowired private EquipamentoRepository equipamentoRepository;
    @Autowired private LinhaDeProducaoRepository linhaDeProducaoRepository;
    @Autowired private ChecklistRepository checklistRepository;
    @Autowired private OrdemServicoRepository ordemServicoRepository;
    @Autowired private ModeloEquipamentoRepository modeloRepository;

    // --- ADIÇÃO: Injeção do OrdemServicoController ---
    // Necessário para reutilizar a conversão de OS para DTO no histórico.
    // Lembre-se que o método convertToResponseDTO em OrdemServicoController deve ser público.
    @Autowired private OrdemServicoController ordemServicoController;
    // --- FIM ADIÇÃO ---

    @GetMapping
    public List<EquipamentoResponseDTO> getAllEquipamentos() {
        return equipamentoRepository.findAll().stream()
                .map(this::convertToResponseDTO) // Chama o método corrigido
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
                    // Atualiza dados simples
                    equipamentoExistente.setNome(dto.getNome()); // 'nome' agora é a Tag
                    equipamentoExistente.setCodigo(dto.getCodigo());
                    equipamentoExistente.setCriticidade(dto.getCriticidade());
                    equipamentoExistente.setFrequenciaPreventiva(dto.getFrequenciaPreventiva());
                    equipamentoExistente.setDataUltimaPreventiva(dto.getDataUltimaPreventiva());

                    // Busca Modelo e Linha (com tratamento de erro)
                    LinhaDeProducao linha = linhaDeProducaoRepository.findById(dto.getLinhaId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha de Produção não encontrada"));
                    ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo de Equipamento não encontrado"));

                    equipamentoExistente.setLinha(linha);
                    equipamentoExistente.setModelo(modelo); // Seta o novo modelo

                    // Associa Checklist Padrão (se houver)
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
        Optional<Equipamento> equipamentoOptional = equipamentoRepository.findById(id);
        if (equipamentoOptional.isEmpty()) { return ResponseEntity.notFound().build(); }
        try {
            equipamentoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Log do erro pode ser útil
            // log.error("Erro ao deletar equipamento ID {}: {}", id, e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível excluir o equipamento (tag). Verifique se ele não está associado a Ordens de Serviço ou outros registros.");
        }
    }


    // --- CONVERSOR DE DTO PARA ENTIDADE (ATUALIZADO) ---
    private Equipamento convertToEntity(EquipamentoRequestDTO dto) {
        Equipamento equipamento = new Equipamento();
        equipamento.setNome(dto.getNome()); // 'nome' é a Tag
        equipamento.setCodigo(dto.getCodigo());
        equipamento.setCriticidade(dto.getCriticidade());
        equipamento.setFrequenciaPreventiva(dto.getFrequenciaPreventiva());
        equipamento.setDataUltimaPreventiva(dto.getDataUltimaPreventiva());

        // Busca Linha (obrigatória)
        LinhaDeProducao linha = linhaDeProducaoRepository.findById(dto.getLinhaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linha não encontrada com ID: " + dto.getLinhaId()));

        // Busca Modelo (obrigatório)
        ModeloEquipamento modelo = modeloRepository.findById(dto.getModeloId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo não encontrado com ID: " + dto.getModeloId()));

        equipamento.setLinha(linha);
        equipamento.setModelo(modelo); // Seta o Modelo

        if (dto.getChecklistId() != null) {
            Checklist checklist = checklistRepository.findById(dto.getChecklistId()).orElse(null);
            equipamento.setChecklistPadrao(checklist);
        }
        return equipamento;
    }

    /**
     * ATUALIZADO e MAIS SEGURO: Converte Equipamento para DTO,
     * considerando a nova estrutura de Modelo e adicionando null checks.
     */
    private EquipamentoResponseDTO convertToResponseDTO(Equipamento equipamento) {
        if (equipamento == null) return null; // Segurança extra

        EquipamentoResponseDTO dto = new EquipamentoResponseDTO();
        dto.setId(equipamento.getId());
        dto.setNome(equipamento.getNome()); // Nome (Tag)
        dto.setCodigo(equipamento.getCodigo());
        dto.setCriticidade(equipamento.getCriticidade());

        // --- ALTERAÇÃO COM NULL CHECK: Busca dados do Modelo ---
        if (equipamento.getModelo() != null) {
            ModeloEquipamento modelo = equipamento.getModelo();
            dto.setModeloId(modelo.getId());
            dto.setNomeModelo(modelo.getNome());
            dto.setFabricante(modelo.getFabricante());
        } else {
             dto.setNomeModelo("Modelo não associado");
             dto.setFabricante("N/A");
             System.err.println("Aviso: Equipamento ID " + (equipamento.getId() != null ? equipamento.getId() : "NOVO") + " está sem modelo associado.");
        }
        // --- FIM ALTERAÇÃO ---

        dto.setFrequenciaPreventiva(equipamento.getFrequenciaPreventiva());
        dto.setDataUltimaPreventiva(equipamento.getDataUltimaPreventiva());

        // --- LÓGICA DE STATUS (com tratamento de erro) ---
        try {
            // Verifica se ID não é nulo antes de consultar OS
            if (equipamento.getId() != null) {
                boolean osPendenteOuSugerida = ordemServicoRepository.existsByEquipamentoIdAndTipoManutencaoAndStatusIn(
                    equipamento.getId(), "PREVENTIVA",
                    List.of(StatusOrdemServico.SUGESTAO, StatusOrdemServico.AGENDADA, StatusOrdemServico.EM_EXECUCAO, StatusOrdemServico.PENDENTE_DE_CORRECAO)
                );
                if (osPendenteOuSugerida) {
                    dto.setStatusPreventiva("AGENDADA");
                } else if (equipamento.getFrequenciaPreventiva() != null && equipamento.getDataUltimaPreventiva() != null) {
                    LocalDate dataHoje = LocalDate.now();
                    LocalDate proximaPreventiva = equipamento.getDataUltimaPreventiva().plusDays(equipamento.getFrequenciaPreventiva().getDias());
                    long diasParaVencer = ChronoUnit.DAYS.between(dataHoje, proximaPreventiva);
                    if (diasParaVencer < 0) { dto.setStatusPreventiva("ATRASADA"); }
                    else if (diasParaVencer <= 5) { dto.setStatusPreventiva("ATENCAO"); }
                    else { dto.setStatusPreventiva("OK"); }
                } else {
                    dto.setStatusPreventiva("NAO_CONFIGURADA");
                }
            } else {
                 dto.setStatusPreventiva("NAO_CONFIGURADA"); // Equipamento novo ainda não tem OS
            }
        } catch (Exception e) {
             System.err.println("Erro ao calcular status preventiva para Equipamento ID " + (equipamento.getId() != null ? equipamento.getId() : "NOVO") + ": " + e.getMessage());
             dto.setStatusPreventiva("ERRO");
        }
        // --- FIM DA LÓGICA ---

        // --- ALTERAÇÃO COM NULL CHECKS: Linha e Área ---
        if (equipamento.getLinha() != null) {
            LinhaDeProducao linha = equipamento.getLinha();
            dto.setNomeLinha(linha.getNome());
            if (linha.getArea() != null) {
                dto.setNomeArea(linha.getArea().getNome());
            } else {
                 dto.setNomeArea("Área não associada à linha");
                 System.err.println("Aviso: Linha ID " + (linha.getId() != null ? linha.getId() : "NOVA") + " está sem área associada.");
            }
        } else {
            dto.setNomeLinha("Linha não associada");
            dto.setNomeArea("Área não associada");
            System.err.println("Aviso: Equipamento ID " + (equipamento.getId() != null ? equipamento.getId() : "NOVO") + " está sem linha associada.");
        }
        // --- FIM ALTERAÇÃO ---

        // --- ALTERAÇÃO COM NULL CHECK: Checklist ---
        if (equipamento.getChecklistPadrao() != null) {
            Checklist checklist = equipamento.getChecklistPadrao();
            dto.setChecklistNome(checklist.getNome());
            dto.setChecklistId(checklist.getId());
        }
        // --- FIM ALTERAÇÃO ---

        return dto;
    }

    // --- ADIÇÃO: NOVO ENDPOINT PARA HISTÓRICO ---
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<OrdemServicoResponseDTO>> getHistoricoEquipamento(@PathVariable Long id) {
        // 1. Verifica se o equipamento existe
        if (!equipamentoRepository.existsById(id)) {
             return ResponseEntity.ok(Collections.emptyList()); // Retorna lista vazia se não existe
        }

        // 2. Busca as Ordens de Serviço associadas
        List<OrdemServico> historicoOs = ordemServicoRepository.findByEquipamentoIdOrderByDataAgendamentoDesc(id);

        // 3. Converte para DTOs usando o método público do outro controller
        if (historicoOs.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<OrdemServicoResponseDTO> historicoDto = historicoOs.stream()
            // Chama o método PÚBLICO convertToResponseDTO do OrdemServicoController
            .map(os -> ordemServicoController.convertToResponseDTO(os))
            .filter(dto -> dto != null) // Garante que não haja DTOs nulos na lista
            .collect(Collectors.toList());

        // 4. Retorna a lista de DTOs
        return ResponseEntity.ok(historicoDto);
    }
    // --- FIM ADIÇÃO ---
}